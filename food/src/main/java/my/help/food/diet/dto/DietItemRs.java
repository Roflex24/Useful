package my.help.food.diet.dto;

import my.help.food.product.dto.ProductRs;

public record DietItemRs(
        Long id,
        Long productId,
        Double quantity,
        ProductRs product
) {}