package my.help.food.nutrients;

import lombok.Data;
import lombok.Builder;

@Data
@Builder
public class NutrientsExpenditureRs {
    private double bmr;           // базовый обмен (ккал)
    private double stepCalories;  // калории от шагов
    private double totalCalories; // общий расход за день
    private double recommendedCalories;
    private double recommendedProtein;
    private double recommendedCarbohydrate;
    private double recommendedFat;
    private double recommendedFiber;
}