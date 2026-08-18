package my.help.food.nutrients.dto;

import java.time.LocalDate;
import java.util.List;

public record NutrientsRs(
        LocalDate date,
        int calories,
        double protein,
        double fat,
        double carbohydrates,
        double fiber,
        String comment,
        List<ProductsPerDayRs> productsPerDay
) {}