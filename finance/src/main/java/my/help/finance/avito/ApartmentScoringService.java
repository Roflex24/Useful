package my.help.finance.avito;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Считает оценку каждой квартиры относительно ОСТАЛЬНЫХ квартир в переданной
 * подборке (а не по абсолютной шкале) и строит из них топ.
 *
 * Почему относительно подборки, а не абсолютные пороги: "хорошая" цена за м²
 * или "нормальное" время до метро сильно зависят от города и района, а
 * фиксированные пороги пришлось бы постоянно подгонять руками. Процентиль
 * внутри текущей выгрузки сам подстраивается под то, что реально сейчас на
 * рынке — среди 50 квартир в Нижнем Новгороде и среди 50 квартир в Москве
 * "дёшево" будет означать разные абсолютные цифры, а относительный ранг
 * работает одинаково.
 *
 * Как это работает:
 *   1. Для каждого критерия с направлением LOWER/HIGHER_IS_BETTER собираем
 *      "сырые" значения по всем квартирам подборки и переводим их в
 *      процентиль 0..100 (100 — лучшее значение в этой подборке).
 *   2. Для CUSTOM_0_100 критериев (этаж, доверие, свежесть) готовая оценка
 *      0..100 берётся напрямую, без сравнения с другими квартирами.
 *   3. Итоговая оценка квартиры = средневзвешенное всех нормализованных
 *      оценок с весами из запроса, деленное на сумму весов.
 *   4. Квартиры без значения по какому-то критерию получают по нему
 *      нейтральную оценку 50 — это не наказывает и не поощряет.
 */
@Slf4j
@Service
public class ApartmentScoringService {

    /**
     * Считает оценку и строит топ по переданной подборке квартир.
     *
     * @param apartments квартиры, которые нужно сравнить друг с другом
     * @param weights    вес по каждому критерию (ключи — см. {@link ScoringCriterion#getKey()});
     *                   критерии с весом 0 или не указанные в карте в итоговую
     *                   оценку не входят, но показываются в разбивке.
     */
    public List<ApartmentScoreResult> scoreAndRank(List<Apartment> apartments, Map<String, Double> weights) {
        if (apartments == null || apartments.isEmpty()) {
            return List.of();
        }

        Map<String, Double> effectiveWeights = normalizeWeights(weights);

        // Шаг 1: для каждого критерия — карта apartmentId -> нормализованная оценка 0..100
        Map<ScoringCriterion, Map<Long, Double>> normalizedByCriterion = new LinkedHashMap<>();
        for (ScoringCriterion criterion : ScoringCriterion.values()) {
            normalizedByCriterion.put(criterion, normalizeCriterion(apartments, criterion));
        }

        // Шаг 2: считаем итог по каждой квартире
        List<ApartmentScoreResult> results = new ArrayList<>();
        for (Apartment apt : apartments) {
            List<CriterionScore> breakdown = new ArrayList<>();
            double weightedSum = 0;
            double weightSum = 0;

            for (ScoringCriterion criterion : ScoringCriterion.values()) {
                double weight = effectiveWeights.getOrDefault(criterion.getKey(), 0.0);
                Double normalized = normalizedByCriterion.get(criterion).get(apt.getId());
                Double raw = criterion.extract(apt);

                breakdown.add(new CriterionScore(
                        criterion.getKey(),
                        criterion.getDescription(),
                        raw,
                        normalized,
                        weight,
                        normalized != null ? normalized * weight : 0.0
                ));

                if (weight > 0 && normalized != null) {
                    weightedSum += normalized * weight;
                    weightSum += weight;
                }
            }

            Double totalScore = weightSum > 0 ? round2(weightedSum / weightSum) : null;

            results.add(new ApartmentScoreResult(
                    0, // ранг проставим после сортировки
                    apt.getId(),
                    apt.getAvitoId(),
                    apt.getTitle(),
                    apt.getPrice(),
                    apt.getUrl(),
                    totalScore,
                    breakdown
            ));
        }

        // Шаг 3: сортировка по убыванию оценки (квартиры без оценки — в конец) и проставление ранга
        results.sort(Comparator.comparing(
                (ApartmentScoreResult r) -> r.totalScore() == null ? Double.NEGATIVE_INFINITY : r.totalScore()
        ).reversed());

        List<ApartmentScoreResult> ranked = new ArrayList<>(results.size());
        for (int i = 0; i < results.size(); i++) {
            ApartmentScoreResult r = results.get(i);
            ranked.add(new ApartmentScoreResult(
                    i + 1, r.apartmentId(), r.avitoId(), r.title(), r.price(), r.url(), r.totalScore(), r.breakdown()
            ));
        }

        if (effectiveWeights.values().stream().allMatch(w -> w == 0.0)) {
            log.warn("Все веса равны 0 — итоговая оценка не будет посчитана (totalScore=null у всех квартир).");
        }

        return ranked;
    }

    /** Игнорирует неизвестные ключи и отрицательные веса (заменяет на 0), не мутирует вход. */
    private Map<String, Double> normalizeWeights(Map<String, Double> weights) {
        Map<String, Double> result = new HashMap<>();
        if (weights == null) return result;
        for (Map.Entry<String, Double> e : weights.entrySet()) {
            ScoringCriterion criterion = ScoringCriterion.byKey(e.getKey());
            if (criterion == null) {
                log.warn("Неизвестный ключ критерия в весах: '{}' — проигнорирован. Доступные ключи: {}",
                        e.getKey(), Arrays.toString(ScoringCriterion.values()));
                continue;
            }
            double w = e.getValue() == null ? 0.0 : Math.max(0.0, e.getValue());
            result.put(criterion.getKey(), w);
        }
        return result;
    }

    /**
     * Нормализует один критерий по всей подборке в оценку 0..100 на квартиру.
     * Для CUSTOM_0_100 критериев (этаж/доверие/свежесть) — просто возвращает
     * готовую оценку extractor'а (с заменой null на нейтральные 50).
     * Для LOWER/HIGHER_IS_BETTER — переводит сырые значения в процентиль.
     */
    private Map<Long, Double> normalizeCriterion(List<Apartment> apartments, ScoringCriterion criterion) {
        Map<Long, Double> result = new HashMap<>();

        if (criterion.getDirection() == ScoringCriterion.RankDirection.CUSTOM_0_100) {
            for (Apartment apt : apartments) {
                Double v = criterion.extract(apt);
                result.put(apt.getId(), v == null ? 50.0 : clamp(v));
            }
            return result;
        }

        // Собираем валидные (не null) значения для процентильного ранжирования
        List<Map.Entry<Long, Double>> valid = new ArrayList<>();
        for (Apartment apt : apartments) {
            Double v = criterion.extract(apt);
            if (v != null) valid.add(Map.entry(apt.getId(), v));
        }

        // Квартиры без значения по этому критерию — нейтральная оценка
        Set<Long> validIds = new HashSet<>();
        for (var e : valid) validIds.add(e.getKey());
        for (Apartment apt : apartments) {
            if (!validIds.contains(apt.getId())) {
                result.put(apt.getId(), 50.0);
            }
        }

        int n = valid.size();
        if (n <= 1) {
            // Сравнивать не с чем — всем валидным тоже нейтральная оценка
            for (var e : valid) result.put(e.getKey(), 50.0);
            return result;
        }

        // Сортируем по значению по возрастанию и считаем ранг с усреднением при равных значениях
        valid.sort(Comparator.comparingDouble(Map.Entry::getValue));

        int i = 0;
        while (i < n) {
            int j = i;
            while (j + 1 < n && Double.compare(valid.get(j + 1).getValue(), valid.get(i).getValue()) == 0) {
                j++;
            }
            // Усреднённый ранг для группы связанных значений [i..j] (0-индексация)
            double avgRank = (i + j) / 2.0;
            double percentile = avgRank / (n - 1); // 0..1, 0 = наименьшее значение в подборке

            double score = criterion.getDirection() == ScoringCriterion.RankDirection.LOWER_IS_BETTER
                    ? (1 - percentile) * 100.0   // наименьшее значение (напр. самая низкая цена) -> 100
                    : percentile * 100.0;        // наибольшее значение (напр. самая большая площадь) -> 100

            for (int k = i; k <= j; k++) {
                result.put(valid.get(k).getKey(), round2(score));
            }
            i = j + 1;
        }

        return result;
    }

    private double clamp(double v) {
        return Math.max(0.0, Math.min(100.0, v));
    }

    private double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
