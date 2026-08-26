package my.help.finance.avito.dto;

import java.util.List;

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
