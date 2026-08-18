package my.help.food.nutrients.dto;

import my.help.food.common.enums.Shop;

public record ProductsPerDayRs(
        Long id,
        String name,
        Double calories,
        Double protein,
        Double fat,
        Double carbohydrate,
        Double fiber,
        String unit,
        String photoUrl,
        Shop shop,
        Double quantity
) {}