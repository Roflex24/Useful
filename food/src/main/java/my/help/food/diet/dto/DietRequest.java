package my.help.food.diet.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record DietRequest(
        @NotBlank(message = "Название рациона обязательно")
        @Size(max = 150)
        String name,

        @Size(max = 1000)
        String description,

        @Valid
        List<DietItemRequest> items
) {}