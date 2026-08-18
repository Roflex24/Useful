package my.help.food.nutrients.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public record NutrientsRequest(
        @NotNull @PastOrPresent LocalDate date,
        @Size(max = 2000) String comment,
        @Valid List<ProductsPerDayRequest> productsPerDay
) {}