package my.help.food.diet.dto;

import java.util.List;

public record DietResponse(
        Long id,
        String name,
        String description,
        List<DietItemRs> items
) {}