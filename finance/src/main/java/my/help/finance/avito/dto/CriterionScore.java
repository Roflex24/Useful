package my.help.finance.avito.dto;

/**
 * Вклад одного критерия в итоговую оценку квартиры.
 * <p>
 * rawValue         — исходное значение (например, 133581.0 для цены за м²);
 *                     null, если данных не было (использована нейтральная оценка).
 * normalizedScore  — приведённая оценка 0..100 (для LOWER/HIGHER_IS_BETTER —
 *                     процентиль относительно остальных квартир в подборке).
 * weight           — вес, с которым критерий взят из запроса (0, если не указан/исключён).
 * weightedContribution — normalizedScore * weight, вклад в сумму до деления на sum(weight).
 */
public record CriterionScore(
        String criterion,
        String description,
        Double rawValue,
        Double normalizedScore,
        Double weight,
        Double weightedContribution
) {}
