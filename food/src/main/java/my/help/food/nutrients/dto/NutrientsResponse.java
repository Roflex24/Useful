package my.help.food.nutrients.dto;

import java.time.LocalDate;
import java.util.List;

public record NutrientsResponse(
        LocalDate date,
        int calories,
        double protein,
        double fat,
        double carbohydrates,
        double fiber,
        String comment,
        List<ProductsPerDayResponse> productsPerDay
) {}