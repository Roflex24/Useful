package my.help.finance.avito.dto;

public record CriterionScore(
        String criterion,
        String description,
        Double rawValue,
        Double normalizedScore,
        Double weight,
        Double weightedContribution
) {}
