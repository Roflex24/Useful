package my.help.food.nutrients.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record NutrientsUpdateRq(
        @Size(max = 2000) String comment,

        @NotNull(message = "Калории обязательны")
        @PositiveOrZero(message = "Калории не могут быть отрицательными")
        Integer calories,

        @NotNull(message = "Белки обязательны")
        @PositiveOrZero(message = "Белки не могут быть отрицательными")
        Double protein,

        @NotNull(message = "Жиры обязательны")
        @PositiveOrZero(message = "Жиры не могут быть отрицательными")
        Double fat,

        @NotNull(message = "Углеводы обязательны")
        @PositiveOrZero(message = "Углеводы не могут быть отрицательными")
        Double carbohydrates,

        @NotNull(message = "Клетчатка обязательна")
        @PositiveOrZero(message = "Клетчатка не может быть отрицательной")
        Double fiber,

        @Valid List<ProductsPerDayRq> productsPerDay
) {}