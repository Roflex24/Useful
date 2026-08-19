package my.help.food.nutrients;

import my.help.food.common.enums.Shop;
import java.time.LocalDate;

public record NutrientsWithProductsRowProjection(
        LocalDate date,
        int calories,
        double protein,
        double fat,
        double carbohydrates,
        double fiber,
        String comment,
        Long productId,
        String productName,
        Double productCalories,
        Double productProtein,
        Double productFat,
        Double productCarbohydrate,
        Double productFiber,
        String unit,
        String photoUrl,
        Shop shop,
        Double quantity
) {}