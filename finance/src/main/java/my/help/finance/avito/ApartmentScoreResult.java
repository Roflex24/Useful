package my.help.finance.avito;

import java.util.List;

/**
 * Итог ранжирования одной квартиры: место в топе, итоговая оценка 0..100
 * и разбивка по критериям (чтобы было видно, из чего сложилась оценка).
 */
public record ApartmentScoreResult(
        int rank,
        Long apartmentId,
        String avitoId,
        String title,
        Long price,
        String url,
        Double totalScore,
        List<CriterionScore> breakdown
) {}
