package my.help.food.diet.dto;

public record DietItemResponse(
        Long id,
        Long productId,
        Double quantity
) {}