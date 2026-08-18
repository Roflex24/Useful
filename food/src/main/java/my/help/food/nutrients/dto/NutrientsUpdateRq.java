package my.help.food.nutrients.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record NutrientsUpdateRq(
        @Size(max = 2000) String comment,
        @Valid List<ProductsPerDayRq> productsPerDay
) {}