package my.help.food.nutrients.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ProductsPerDayRequest(
        @NotNull Long productId,
        @NotNull @Positive Double quantity
) {}