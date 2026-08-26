package my.help.finance.avito.dto;

import java.util.Map;

public record ScoringRq(Map<String, Double> weights) {

    public ScoringRq {
        if (weights == null) {
            weights = Map.of();
        }
    }
}
