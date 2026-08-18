package my.help.food.product.dto;

import my.help.food.common.enums.Shop;

public record ProductRs(
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