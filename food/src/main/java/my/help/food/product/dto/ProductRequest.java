package my.help.food.product.dto;

import jakarta.validation.constraints.*;
import my.help.food.common.enums.Shop;

public record ProductRequest(
        @NotBlank(message = "Название обязательно")
        @Size(max = 150, message = "Название не длиннее 150 символов")
        String name,

        @PositiveOrZero(message = "Калории не могут быть отрицательными")
        double calories,

        @PositiveOrZero(message = "Белки не могут быть отрицательными")
        double protein,

        @PositiveOrZero(message = "Жиры не могут быть отрицательными")
        double fat,

        @PositiveOrZero(message = "Углеводы не могут быть отрицательными")
        double carbohydrate,

        @PositiveOrZero(message = "Клетчатка не может быть отрицательной")
        double fiber,

        @NotBlank(message = "Единица измерения обязательна")
        @Pattern(regexp = "шт|100г", message = "Допустимые единицы: шт или 100г")
        String unit,

        @Size(max = 500, message = "URL фото слишком длинный")
        String photoUrl,

        Shop shop
) {}