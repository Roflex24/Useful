package my.help.finance.avito.dto;

import lombok.Getter;
import my.help.finance.avito.entity.Apartment;
import my.help.finance.avito.entity.ApartmentBadge;

import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Критерии, по которым считается оценка квартиры. Каждый критерий:
 *   - имеет строковый {@link #key} — именно под этим ключом передаётся
 *     вес в запросе на ранжирование ({"pricePerMeter": 3, ...});
 *   - имеет {@link #direction} — как нормализовать значение в 0..100;
 *   - имеет {@link #extractor} — как достать "сырое" значение из Apartment
 *     (для LOWER_IS_BETTER/HIGHER_IS_BETTER — это метрика, которая потом
 *     превращается в процентиль относительно остальных квартир в подборке;
 *     для CUSTOM_0_100 — extractor уже возвращает готовую оценку 0..100).
 * <p>
 * Если extractor вернул null (данных нет), критерий не наказывает и не
 * поощряет квартиру — ей ставится нейтральная оценка 50.
 */
public enum ScoringCriterion {

    PRICE_PER_METER(
            "pricePerMeter",
            "Цена за м² (дешевле относительно других объявлений в подборке — лучше)",
            RankDirection.LOWER_IS_BETTER,
            apt -> {
                if (apt.getPricePerMeter() != null) return apt.getPricePerMeter().doubleValue();
                if (apt.getPrice() != null && apt.getTotalArea() != null && apt.getTotalArea() > 0) {
                    return apt.getPrice() / apt.getTotalArea();
                }
                return null;
            }
    ),

    PRICE_TOTAL(
            "priceTotal",
            "Полная цена (дешевле — лучше)",
            RankDirection.LOWER_IS_BETTER,
            apt -> apt.getPrice() != null ? apt.getPrice().doubleValue() : null
    ),

    AREA(
            "area",
            "Площадь (больше — лучше)",
            RankDirection.HIGHER_IS_BETTER,
            Apartment::getTotalArea
    ),

    METRO_DISTANCE(
            "metroDistance",
            "Время до метро пешком (ближе — лучше; если метро не указано — нейтрально)",
            RankDirection.LOWER_IS_BETTER,
            apt -> apt.getMetroMinutes() != null ? apt.getMetroMinutes().doubleValue() : null
    ),

    SELLER_EXPERIENCE(
            "sellerExperience",
            "Опыт продавца (число завершённых объявлений — больше — лучше)",
            RankDirection.HIGHER_IS_BETTER,
            apt -> apt.getSellerCompletedListingsCount() != null
                    ? apt.getSellerCompletedListingsCount().doubleValue() : null
    ),

    FLOOR_POSITION(
            "floorPosition",
            "Позиция этажа (средние этажи лучше первого и последнего)",
            RankDirection.CUSTOM_0_100,
            ScoringCriterion::floorPositionScore
    ),

    TRUST(
            "trust",
            "Доверие: бейджи проверки продавца и объявления",
            RankDirection.CUSTOM_0_100,
            ScoringCriterion::trustScore
    ),

    FRESHNESS(
            "freshness",
            "Свежесть объявления (недавно опубликовано — лучше)",
            RankDirection.CUSTOM_0_100,
            ScoringCriterion::freshnessScore
    ),

    CEILING_HEIGHT(
            "ceilingHeight",
            "Высота потолков (выше — лучше)",
            RankDirection.HIGHER_IS_BETTER,
            Apartment::getCeilingHeight
    ),

    BUILDING_AGE(
            "buildingAge",
            "Год постройки дома (новее дом — лучше)",
            RankDirection.HIGHER_IS_BETTER,
            apt -> apt.getYearBuilt() != null ? apt.getYearBuilt().doubleValue() : null
    ),

    HOUSE_RATING(
            "houseRating",
            "Рейтинг дома по отзывам жильцов (выше — лучше)",
            RankDirection.HIGHER_IS_BETTER,
            Apartment::getHouseRating
    ),

    RENOVATION_QUALITY(
            "renovationQuality",
            "Качество ремонта (по формулировке Авито: дизайнерский/евро/косметический/без ремонта)",
            RankDirection.CUSTOM_0_100,
            ScoringCriterion::renovationQualityScore
    ),

    ROSREESTR_CHECK(
            "rosreestrCheck",
            "Проверка в Росреестре: совпадение площади/адреса/этажа и отсутствие ограничений",
            RankDirection.CUSTOM_0_100,
            ScoringCriterion::rosreestrCheckScore
    );

    public enum RankDirection { LOWER_IS_BETTER, HIGHER_IS_BETTER, CUSTOM_0_100 }

    @Getter
    private final String key;
    @Getter
    private final String description;
    @Getter
    private final RankDirection direction;
    private final Function<Apartment, Double> extractor;

    ScoringCriterion(String key, String description, RankDirection direction,
                     Function<Apartment, Double> extractor) {
        this.key = key;
        this.description = description;
        this.direction = direction;
        this.extractor = extractor;
    }

    public Double extract(Apartment apt) { return extractor.apply(apt); }

    public static ScoringCriterion byKey(String key) {
        for (ScoringCriterion c : values()) {
            if (c.key.equalsIgnoreCase(key)) return c;
        }
        return null;
    }

    // ------------------------------------------------------------------
    // CUSTOM_0_100 критерии — сами возвращают готовую оценку 0..100
    // ------------------------------------------------------------------

    /**
     * Средние этажи считаются лучше: первый этаж менее желателен
     * (шум подъезда, риск проникновения, влажность), последний — компромисс
     * (вид, но риск течи крыши). Это осознанно упрощённая эвристика —
     * поменяйте значения ниже, если ваши предпочтения другие.
     */
    private static Double floorPositionScore(Apartment apt) {
        Integer floor = apt.getFloor();
        Integer total = apt.getTotalFloors();
        if (floor == null || total == null || total <= 1) return null;
        if (floor <= 1) return 30.0;
        if (floor.intValue() == total.intValue()) return 60.0;
        return 100.0;
    }

    private static Double trustScore(Apartment apt) {
        Set<ApartmentBadge> badges = apt.getBadges();
        if (badges == null || badges.isEmpty()) return 0.0;

        double score = 0;
        for (ApartmentBadge b : badges) {
            String label = b.getLabel() == null ? "" : b.getLabel();
            switch (label) {
                case "Проверено в Росреестре" -> score += 30;
                case "Реквизиты проверены" -> score += 25;
                case "Документы проверены" -> score += 20;
                case "Данные подтверждены" -> score += 20;
                case "Надёжный партнёр" -> score += 15;
                case "Собственник" -> score += 10;
                default -> { /* прочие бейджи ("Кирпичный дом" и т.п.) на доверие не влияют */ }
            }
        }
        return Math.min(100.0, score);
    }

    /**
     * Формулировка ремонта у Авито — свободный текст, не перечисление, поэтому
     * матчим по ключевым словам. Формулировка не распознана или отсутствует —
     * null, критерий не наказывает и не поощряет квартиру (нейтральные 50).
     */
    private static Double renovationQualityScore(Apartment apt) {
        String renovation = apt.getRenovation();
        if (renovation == null || renovation.isBlank()) return null;
        String r = renovation.toLowerCase();

        if (r.contains("дизайнерск")) return 100.0;
        if (r.contains("евро")) return 85.0;
        if (r.contains("хорош") || r.contains("частичн")) return 65.0;
        if (r.contains("косметическ")) return 55.0;
        if (r.contains("требует ремонта") || r.contains("без ремонта")) return 15.0;
        return 50.0; // формулировка не распознана — нейтрально
    }

    /**
     * Собирает {@link Apartment#getRosreestrDataMatches()} и
     * {@link Apartment#getRosreestrHasRestrictions()} в единую оценку 0..100.
     * Если проверки не было вовсе (оба флага null) — null, чтобы отдельно от
     * этого критерия квартира получила нейтральную оценку, а не штраф за
     * то, что бот ещё не обошёл объявление.
     */
    private static Double rosreestrCheckScore(Apartment apt) {
        Boolean matches = apt.getRosreestrDataMatches();
        Boolean restrictions = apt.getRosreestrHasRestrictions();
        if (matches == null && restrictions == null) return null;

        double score = 50.0;
        if (Boolean.TRUE.equals(matches)) score += 20;
        else if (Boolean.FALSE.equals(matches)) score -= 25;

        if (Boolean.FALSE.equals(restrictions)) score += 30;
        else if (Boolean.TRUE.equals(restrictions)) score -= 40;

        return Math.max(0.0, Math.min(100.0, score));
    }

    private static final Pattern DAYS_AGO = Pattern.compile("(\\d+)\\s*д[ен]");

    private static Double freshnessScore(Apartment apt) {
        String raw = apt.getPublishedDateRaw();
        double score;
        if (raw == null || raw.isBlank()) {
            score = 50.0;
        } else if (raw.contains("час") || raw.contains("минут") || raw.equalsIgnoreCase("Сегодня")) {
            score = 100.0;
        } else if (raw.equalsIgnoreCase("Вчера")) {
            score = 90.0;
        } else {
            Matcher m = DAYS_AGO.matcher(raw);
            if (m.find()) {
                int days = Integer.parseInt(m.group(1));
                score = Math.max(0.0, 100.0 - days * 10.0);
            } else {
                score = 50.0; // формат не распознан — нейтрально
            }
        }
        if (Boolean.TRUE.equals(apt.getIsNew())) {
            score = Math.min(100.0, score + 10.0);
        }
        return score;
    }
}