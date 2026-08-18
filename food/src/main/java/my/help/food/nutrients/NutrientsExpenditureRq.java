package my.help.food.nutrients;

import jakarta.validation.constraints.*;

public record NutrientsExpenditureRq(
        @NotBlank(message = "Цель обязательна")
        String target,

        @NotBlank(message = "Пол обязателен")
        @Pattern(regexp = "male|female", message = "Пол должен быть male или female")
        String gender,

        @Positive(message = "Вес должен быть положительным")
        double weightKg,

        @Positive(message = "Рост должен быть положительным")
        double heightCm,

        @Positive(message = "Возраст должен быть положительным")
        int ageYears,

        @PositiveOrZero(message = "Шаги не могут быть отрицательными")
        int steps
) {}