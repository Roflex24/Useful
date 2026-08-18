package my.help.food.nutrients;

import my.help.food.common.exception.ProductNotFoundException;
import my.help.food.product.dto.ProductRs;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class NutritionCalculator {

    public record NutritionTotals(int calories, double protein, double fat,
                                  double carbohydrates, double fiber) {
    }

    public NutritionTotals calculate(Map<Long, Double> quantityByProductId,
                                     Map<Long, ProductRs> productsById) {
        double calories = 0, protein = 0, fat = 0, carbs = 0, fiber = 0;

        for (Map.Entry<Long, Double> entry : quantityByProductId.entrySet()) {
            Long productId = entry.getKey();
            double quantity = entry.getValue();

            ProductRs product = productsById.get(productId);
            if (product == null) {
                throw new ProductNotFoundException(productId);
            }

            double multiplier = "100г".equals(product.unit())
                    ? quantity / 100.0
                    : quantity;

            calories += product.calories() * multiplier;
            protein += product.protein() * multiplier;
            fat += product.fat() * multiplier;
            carbs += product.carbohydrate() * multiplier;
            fiber += product.fiber() * multiplier;
        }

        return new NutritionTotals(
                (int) Math.round(calories),
                round(protein),
                round(fat),
                round(carbs),
                round(fiber)
        );
    }

    private double round(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}