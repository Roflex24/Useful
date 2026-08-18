package my.help.food.product.dto;

import my.help.food.product.Shop;

public record ProductResponse(
        Long id,
        String name,
        double calories,
        double protein,
        double fat,
        double carbohydrate,
        double fiber,
        String unit,
        String photoUrl,
        Shop shop
) {}