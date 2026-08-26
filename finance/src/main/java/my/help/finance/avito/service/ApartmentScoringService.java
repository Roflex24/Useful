package my.help.finance.avito.service;

import lombok.extern.slf4j.Slf4j;
import my.help.finance.avito.dto.CriterionScore;
import my.help.finance.avito.dto.ScoringCriterion;
import my.help.finance.avito.dto.ApartmentScoreResult;
import my.help.finance.avito.entity.Apartment;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class ApartmentScoringService {

    public List<ApartmentScoreResult> scoreAndRank(List<Apartment> apartments, Map<String, Double> weights) {
        if (apartments == null || apartments.isEmpty()) {
            return List.of();
        }

        Map<String, Double> effectiveWeights = normalizeWeights(weights);

        Map<ScoringCriterion, Map<Long, Double>> normalizedByCriterion = new LinkedHashMap<>();
        for (ScoringCriterion criterion : ScoringCriterion.values()) {
            normalizedByCriterion.put(criterion, normalizeCriterion(apartments, criterion));
        }

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

    private Map<Long, Double> normalizeCriterion(List<Apartment> apartments, ScoringCriterion criterion) {
        Map<Long, Double> result = new HashMap<>();

        if (criterion.getDirection() == ScoringCriterion.RankDirection.CUSTOM_0_100) {
            for (Apartment apt : apartments) {
                Double v = criterion.extract(apt);
                result.put(apt.getId(), v == null ? 50.0 : clamp(v));
            }
            return result;
        }

        List<Map.Entry<Long, Double>> valid = new ArrayList<>();
        for (Apartment apt : apartments) {
            Double v = criterion.extract(apt);
            if (v != null) valid.add(Map.entry(apt.getId(), v));
        }

        Set<Long> validIds = new HashSet<>();
        for (var e : valid) validIds.add(e.getKey());
        for (Apartment apt : apartments) {
            if (!validIds.contains(apt.getId())) {
                result.put(apt.getId(), 50.0);
            }
        }

        int n = valid.size();
        if (n <= 1) {
            for (var e : valid) result.put(e.getKey(), 50.0);
            return result;
        }

        valid.sort(Comparator.comparingDouble(Map.Entry::getValue));

        int i = 0;
        while (i < n) {
            int j = i;
            while (j + 1 < n && Double.compare(valid.get(j + 1).getValue(), valid.get(i).getValue()) == 0) {
                j++;
            }
            double avgRank = (i + j) / 2.0;
            double percentile = avgRank / (n - 1);

            double score = criterion.getDirection() == ScoringCriterion.RankDirection.LOWER_IS_BETTER
                    ? (1 - percentile) * 100.0
                    : percentile * 100.0;

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
