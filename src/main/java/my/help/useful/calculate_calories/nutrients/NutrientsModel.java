package my.help.useful.calculate_calories.nutrients;

import lombok.*;
import my.help.useful.calculate_calories.products_per_day.ProductsPerDayModel;

import java.time.LocalDate;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class NutrientsModel {

    private LocalDate date;
    private int calories;
    private double protein;
    private double fat;
    private double carbohydrates;
    private double fiber;

    private List<ProductsPerDayModel> productsPerDay;
}
