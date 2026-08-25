package my.help.finance.avito.dto;

import java.util.Map;

/**
 * Запрос на ранжирование квартир.
 * <p>
 * weights — ключ критерия -&gt; вес (любое неотрицательное число, шкала
 * произвольная — важны только соотношения весов друг к другу).
 * Критерий с весом 0 или отсутствующий в карте в расчёт итоговой оценки
 * не идёт, но в разбивке (breakdown) по каждой квартире всё равно
 * показывается — так видно, что учли и что осознанно исключили.
 * <p>
 * Доступные ключи критериев: pricePerMeter, priceTotal, area,
 * metroDistance, sellerExperience, floorPosition, trust, freshness
 * (см. {@link ScoringCriterion}).
 * <p>
 * Пример тела запроса — "ищем дешёвые, но проверенные квартиры рядом с метро":
 * {
 *   "weights": {
 *     "pricePerMeter": 3,
 *     "metroDistance": 2,
 *     "trust": 2,
 *     "floorPosition": 1
 *   }
 * }
 */
public record ScoringRq(Map<String, Double> weights) {

    public ScoringRq {
        if (weights == null) {
            weights = Map.of();
        }
    }
}
