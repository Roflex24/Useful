package my.help.food.nutrients;

import org.springframework.stereotype.Component;

@Component
public class NutritionCalculator {

    public NutrientsExpenditureRs calculateDailyNutrients(NutrientsExpenditureRq request) {
        double bmr = calculateBMR(request);
        double stepCalories = request.steps() * (request.weightKg() * 0.0005);
        double totalCalories = bmr + stepCalories;
        double targetCalories = calculateTargetCalories(totalCalories, request.target());
        NutritionValues nutrition = calculateNutrition(targetCalories, request.weightKg(), request.target());

        return NutrientsExpenditureRs.builder()
                .bmr(round(bmr))
                .stepCalories(round(stepCalories))
                .totalCalories(round(totalCalories))
                .recommendedCalories(round(targetCalories))
                .recommendedProtein(nutrition.protein)
                .recommendedFat(nutrition.fat)
                .recommendedCarbohydrate(nutrition.carbohydrate)
                .recommendedFiber(nutrition.fiber)
                .build();
    }

    private double calculateBMR(NutrientsExpenditureRq request) {
        boolean isMale = "male".equalsIgnoreCase(request.gender());
        double bmr;
        if (isMale) {
            bmr = (10 * request.weightKg()) + (6.25 * request.heightCm()) - (5 * request.ageYears()) + 5;
        } else {
            bmr = (10 * request.weightKg()) + (6.25 * request.heightCm()) - (5 * request.ageYears()) - 161;
        }
        return bmr;
    }

    private double calculateTargetCalories(double tdee, String target) {
        return switch (target) {
            case "Похудение (агрессивное)" -> tdee - 500;
            case "Похудение (мягкое)" -> tdee - 250;
            case "Рекомпозиция (похудение + рост мышц)" -> tdee - 200;
            case "Поддержание мышц (активный человек, похудение)" -> tdee - 200;
            case "Поддержание веса" -> tdee;
            case "Набор мышечной массы (мягкий)" -> tdee + 200;
            case "Набор мышечной массы (агрессивный)" -> tdee + 400;
            default -> tdee - 200;
        };
    }

    private NutritionValues calculateNutrition(double targetCalories, double weightKg, String target) {
        double proteinMin, proteinMax;
        double fatMin, fatMax;
        double protein, fat, carbohydrate;
        int fiber;

        proteinMax = switch (target) {
            case "Набор мышечной массы (мягкий)", "Набор мышечной массы (агрессивный)" -> {
                proteinMin = 1.6;
                yield 2.2;
            }
            case "Рекомпозиция (похудение + рост мышц)", "Поддержание мышц (активный человек, похудение)" -> {
                proteinMin = 1.6;
                yield 2.0;
            }
            case "Похудение (агрессивное)", "Похудение (мягкое)" -> {
                proteinMin = 1.8;
                yield 2.2;
            }
            default -> {
                proteinMin = 1.2;
                yield 1.6;
            }
        };

        protein = Math.round((proteinMin + proteinMax) / 2 * weightKg * 10.0) / 10.0;

        fatMax = switch (target) {
            case "Похудение (агрессивное)" -> {
                fatMin = 0.6;
                yield 0.8;
            }
            case "Похудение (мягкое)", "Рекомпозиция (похудение + рост мышц)",
                 "Поддержание мышц (активный человек, похудение)" -> {
                fatMin = 0.8;
                yield 1.0;
            }
            default -> {
                fatMin = 0.8;
                yield 1.2;
            }
        };

        fat = Math.round((fatMin + fatMax) / 2 * weightKg * 10.0) / 10.0;

        double proteinCalories = protein * 4;
        double fatCalories = fat * 9;
        double remainingCalories = targetCalories - proteinCalories - fatCalories;

        if (remainingCalories < 0) {
            fat = Math.round((targetCalories * 0.25) / 9 * 10.0) / 10.0;
            fatCalories = fat * 9;
            protein = Math.round((targetCalories * 0.30) / 4 * 10.0) / 10.0;
            proteinCalories = protein * 4;
            remainingCalories = targetCalories - proteinCalories - fatCalories;
        }

        carbohydrate = Math.round(remainingCalories / 4 * 10.0) / 10.0;
        if (carbohydrate < 0) carbohydrate = 0;

        if (target.contains("Похудение") && target.contains("агрессивное")) {
            fiber = 25;
        } else {
            fiber = 30;
        }

        return new NutritionValues(protein, fat, carbohydrate, fiber);
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private record NutritionValues(double protein, double fat, double carbohydrate, int fiber) {}
}