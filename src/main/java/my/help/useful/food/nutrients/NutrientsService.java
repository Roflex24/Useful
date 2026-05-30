package my.help.useful.food.nutrients;

import lombok.RequiredArgsConstructor;
import my.help.useful.food.products_per_day.ProductsPerDayEntity;
import my.help.useful.food.products_per_day.ProductsPerDayMapper;
import my.help.useful.food.products_per_day.ProductsPerDayService;
import my.help.useful.food.products_per_day.ProductsPerDayModel;
import my.help.useful.food.product.ProductModel;
import my.help.useful.food.product.ProductService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class NutrientsService {

    private final NutrientsRepository nutrientsRepository;
    private final NutrientsMapper nutrientsMapper;
    private final ProductsPerDayService productsPerDayService;
    private final ProductService productService;
    private final ProductsPerDayMapper productsPerDayMapper;


    public NutrientsModel addNutrientsPerDay(NutrientsModel nutrientsModel) {
        nutrientsRepository.save(nutrientsMapper.toEntity(nutrientsModel));
        Map<Long, Double> productQuantityMap = new HashMap<>();
        for (ProductsPerDayModel productsPerDayModel : nutrientsModel.getProductsPerDay()) {
            productQuantityMap.put(productsPerDayModel.getId(), productsPerDayModel.getQuantity());
        }
        productsPerDayService.addProductsPerDay(nutrientsModel.getDate(), productQuantityMap);
        return nutrientsModel;
    }

    public List<NutrientsModel> getNutrientsList() {
        return nutrientsMapper.toModelList(nutrientsRepository.findAll());
    }

    public NutrientsModel getNutrientsPerDate(LocalDate localDate) {
        NutrientsPerDayEntity nutrientsPerDayEntity = null;
        if (nutrientsRepository.findById(localDate).isPresent()) {
            nutrientsPerDayEntity = nutrientsRepository.findById(localDate).get();
        } else {
            return null;
        }
        NutrientsModel nutrientsModel = nutrientsMapper.toModel(nutrientsPerDayEntity);
        List<ProductsPerDayEntity> productsPerDayEntityList = productsPerDayService.getProductsPerDate(localDate);
        List<ProductsPerDayModel> productsPerDayModelList = new ArrayList<>();
        for (ProductsPerDayEntity productsPerDayEntity : productsPerDayEntityList) {
            ProductModel productModel = productService.getProductById(productsPerDayEntity.getId().getProductId());
            productsPerDayModelList.add(productsPerDayMapper.toModel(productModel, productsPerDayEntity.getQuantity()));
        }
        productsPerDayModelList.sort(Comparator.comparing(ProductsPerDayModel::getName));
        nutrientsModel.setProductsPerDay(productsPerDayModelList);
        return nutrientsModel;
    }

    public List<NutrientsModel> getNutrientsPerDateForWeek() {
        List<NutrientsModel> list = new ArrayList<>();
        for (int i=0; i<=7; i++) {
            list.add(getNutrientsPerDate(LocalDate.now().minusDays(i)));
        }
        return list;
    }


    public NutrientsExpenditureRs calculateDailyNutrients(NutrientsExpenditureRq rq) {
        // 1. Расчёт базового обмена (BMR) по формуле Миффлина - Сан-Жеора
        double bmr = calculateBMR(rq);

        // 2. Расчёт калорий от шагов (1 шаг ≈ 0.04 ккал для человека 70 кг)
        // Формула: вес(кг) * 0.0005 * количество шагов
        double stepCalories = rq.getSteps() * (rq.getWeightKg() * 0.0005);

        // 3. Общий расход (TDEE - Total Daily Energy Expenditure)
        double totalCalories = bmr + stepCalories;

        // 4. Определение целевой калорийности в зависимости от цели
        double targetCalories = calculateTargetCalories(totalCalories, rq.getTarget());

        // 5. Расчёт БЖУ и клетчатки на основе целевой калорийности и цели
        NutritionValues nutrition = calculateNutrition(targetCalories, rq.getWeightKg(), rq.getTarget());

        // 6. Формирование ответа
        return NutrientsExpenditureRs.builder()
                .bmr(Math.round(bmr * 100.0) / 100.0)
                .stepCalories(Math.round(stepCalories * 100.0) / 100.0)
                .totalCalories(Math.round(totalCalories * 100.0) / 100.0)
                .recommendedCalories(Math.round(targetCalories * 100.0) / 100.0)
                .recommendedProtein(nutrition.protein)
                .recommendedFat(nutrition.fat)
                .recommendedCarbohydrate(nutrition.carbohydrate)
                .recommendedFiber(nutrition.fiber)
                .build();
    }

    private double calculateBMR(NutrientsExpenditureRq request) {
        boolean isMale = "male".equalsIgnoreCase(request.getGender());

        if (isMale) {
            // Формула для мужчин
            return (10 * request.getWeightKg())
                    + (6.25 * request.getHeightCm())
                    - (5 * request.getAgeYears())
                    + 5;
        } else {
            // Формула для женщин
            return (10 * request.getWeightKg())
                    + (6.25 * request.getHeightCm())
                    - (5 * request.getAgeYears())
                    - 161;
        }
    }

    /**
     * Расчёт целевой калорийности в зависимости от цели
     */
    private double calculateTargetCalories(double tdee, String target) {
        return switch (target) {
            case "Похудение (агрессивное)" -> tdee - 500;
            case "Похудение (мягкое)" -> tdee - 250;
            case "Рекомпозиция (похудение + рост мышц)" -> tdee - 200;
            case "Поддержание мышц (активный человек, похудение)" -> tdee - 200;
            case "Поддержание веса" -> tdee;
            case "Набор мышечной массы (мягкий)" -> tdee + 200;
            case "Набор мышечной массы (агрессивный)" -> tdee + 400;
            default -> tdee - 200; // по умолчанию рекомпозиция
        };
    }

    /**
     * Расчёт БЖУ и клетчатки на основе целевой калорийности и веса
     */
    private NutritionValues calculateNutrition(double targetCalories, double weightKg, String target) {
        double proteinMin, proteinMax;
        double fatMin, fatMax;
        double protein, fat, carbohydrate;
        int fiber;

        // 1. Расчёт белка (на 1 кг веса тела, в зависимости от цели)
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
                yield 2.2; // при похудении белок выше для сохранения мышц
            }
            default -> {
                proteinMin = 1.2;
                yield 1.6; // поддержание веса
            }
        };

        // Берём среднее значение белка
        protein = Math.round((proteinMin + proteinMax) / 2 * weightKg * 10.0) / 10.0;

        // 2. Расчёт жиров (на 1 кг веса тела)
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
                yield 1.2; // поддержание веса и набор массы
            }
        };

        fat = Math.round((fatMin + fatMax) / 2 * weightKg * 10.0) / 10.0;

        // 3. Расчёт углеводов (остаточный принцип: калории - белки - жиры)
        double proteinCalories = protein * 4;
        double fatCalories = fat * 9;
        double remainingCalories = targetCalories - proteinCalories - fatCalories;

        if (remainingCalories < 0) {
            // Если остаток отрицательный, корректируем жиры или белки
            fat = Math.round((targetCalories * 0.25) / 9 * 10.0) / 10.0; // минимум 25% от калорий на жиры
            fatCalories = fat * 9;
            protein = Math.round((targetCalories * 0.30) / 4 * 10.0) / 10.0; // минимум 30% на белки
            proteinCalories = protein * 4;
            remainingCalories = targetCalories - proteinCalories - fatCalories;
        }

        carbohydrate = Math.round(remainingCalories / 4 * 10.0) / 10.0;
        if (carbohydrate < 0) carbohydrate = 0;

        // 4. Расчёт клетчатки (фиксированная норма в зависимости от пола и возраста)
        // Норма для взрослых: мужчины 30-38г, женщины 25-30г
        if (target.contains("Похудение") && target.contains("агрессивное")) {
            // При агрессивном похудении клетчатка может быть на нижней границе
            fiber = 25;
        } else {
            // Стандартная норма
            fiber = 30;
        }

        return new NutritionValues(protein, fat, carbohydrate, fiber);
    }

    /**
     * Вспомогательный класс для хранения рассчитанных значений
     */
    private record NutritionValues(double protein, double fat, double carbohydrate, int fiber) {
    }
}
