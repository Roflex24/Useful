package my.help.food.diet.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record DietItemRq(
        @NotNull(message = "productId обязателен")
        Long productId,

        @NotNull(message = "Количество обязательно")
        @Positive(message = "Количество должно быть положительным")
        Double quantity
) {}