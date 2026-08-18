package my.help.food.nutrients;

import lombok.RequiredArgsConstructor;
import my.help.food.nutrients.dto.*;
import my.help.food.product.ProductNotFoundException;
import my.help.food.product.ProductService;
import my.help.food.product.dto.ProductResponse;
import my.help.food.products_per_day.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class NutrientsService {

    private final NutrientsRepository nutrientsRepository;
    private final NutrientsMapper nutrientsMapper;
    private final ProductsPerDayService productsPerDayService;
    private final ProductService productService;
    private final ProductsPerDayMapper productsPerDayMapper;
    private final NutritionCalculator nutritionCalculator;

    @Transactional
    public NutrientsResponse addNutrientsPerDay(NutrientsRequest request) {
        // Проверяем существование продуктов и считаем КБЖУ на сервере
        Map<Long, Double> productQuantityMap = request.productsPerDay().stream()
                .collect(Collectors.toMap(
                        ProductsPerDayRequest::productId,
                        ProductsPerDayRequest::quantity
                ));

        Map<Long, ProductResponse> productsById = productService.getProductsByIds(productQuantityMap.keySet());

        // Вычисление суммарных КБЖУ
        double calories = 0, protein = 0, fat = 0, carbs = 0, fiber = 0;
        for (Map.Entry<Long, Double> entry : productQuantityMap.entrySet()) {
            ProductResponse product = productsById.get(entry.getKey());
            if (product == null) {
                throw new ProductNotFoundException(entry.getKey());
            }
            double multiplier = "100г".equals(product.unit()) ? entry.getValue() / 100 : entry.getValue();
            calories += product.calories() * multiplier;
            protein += product.protein() * multiplier;
            fat += product.fat() * multiplier;
            carbs += product.carbohydrate() * multiplier;
            fiber += product.fiber() * multiplier;
        }

        NutrientsPerDayEntity entity = new NutrientsPerDayEntity(
                request.date(),
                (int) Math.round(calories),
                round(protein),
                round(fat),
                round(carbs),
                round(fiber),
                request.comment()
        );
        nutrientsRepository.save(entity);

        productsPerDayService.addProductsPerDay(request.date(), productQuantityMap);

        // Формируем ответ
        NutrientsResponse response = nutrientsMapper.toResponse(entity);
        List<ProductsPerDayResponse> ppdResponses = productQuantityMap.entrySet().stream()
                .map(entry -> productsPerDayMapper.toResponse(
                        productsById.get(entry.getKey()),
                        entry.getValue()
                ))
                .sorted(Comparator.comparing(ProductsPerDayResponse::name))
                .toList();
        response = new NutrientsResponse(
                response.date(),
                response.calories(),
                response.protein(),
                response.fat(),
                response.carbohydrates(),
                response.fiber(),
                response.comment(),
                ppdResponses
        );
        return response;
    }

    @Transactional(readOnly = true)
    public List<NutrientsResponse> getNutrientsList() {
        return nutrientsMapper.toResponseList(nutrientsRepository.findAll());
    }

    @Transactional(readOnly = true)
    public NutrientsResponse getNutrientsPerDate(LocalDate date) {
        NutrientsPerDayEntity entity = nutrientsRepository.findById(date)
                .orElseThrow(() -> new NutrientsNotFoundException("Данные за " + date + " не найдены"));

        NutrientsResponse response = nutrientsMapper.toResponse(entity);

        List<ProductsPerDayEntity> ppdEntities = productsPerDayService.getProductsPerDate(date);
        if (!ppdEntities.isEmpty()) {
            List<Long> productIds = ppdEntities.stream()
                    .map(ppd -> ppd.getId().getProductId())
                    .distinct()
                    .toList();
            Map<Long, ProductResponse> productsById = productService.getProductsByIds(productIds);

            List<ProductsPerDayResponse> ppdResponses = ppdEntities.stream()
                    .map(ppd -> {
                        ProductResponse product = productsById.get(ppd.getId().getProductId());
                        if (product == null) {
                            throw new ProductNotFoundException(ppd.getId().getProductId());
                        }
                        return productsPerDayMapper.toResponse(product, ppd.getQuantity());
                    })
                    .sorted(Comparator.comparing(ProductsPerDayResponse::name))
                    .toList();

            response = new NutrientsResponse(
                    response.date(),
                    response.calories(),
                    response.protein(),
                    response.fat(),
                    response.carbohydrates(),
                    response.fiber(),
                    response.comment(),
                    ppdResponses
            );
        }
        return response;
    }

    @Transactional(readOnly = true)
    public List<NutrientsResponse> getNutrientsPerDateForWeek() {
        LocalDate today = LocalDate.now();
        LocalDate start = today.minusDays(6);

        List<NutrientsPerDayEntity> nutrientsEntities = nutrientsRepository.findByDateBetween(start, today);
        Map<LocalDate, NutrientsPerDayEntity> nutrientsByDate = nutrientsEntities.stream()
                .collect(Collectors.toMap(NutrientsPerDayEntity::getDate, Function.identity()));

        List<ProductsPerDayEntity> ppdEntities = productsPerDayService.getProductsBetween(start, today);
        Map<LocalDate, List<ProductsPerDayEntity>> ppdByDate = ppdEntities.stream()
                .collect(Collectors.groupingBy(ppd -> ppd.getId().getDate()));

        List<Long> productIds = ppdEntities.stream()
                .map(ppd -> ppd.getId().getProductId())
                .distinct()
                .toList();
        Map<Long, ProductResponse> productsById = productService.getProductsByIds(productIds);

        return Stream.iterate(start, d -> d.plusDays(1))
                .limit(7)
                .map(date -> {
                    NutrientsPerDayEntity entity = nutrientsByDate.get(date);
                    NutrientsResponse response = entity == null
                            ? new NutrientsResponse(date, 0, 0, 0, 0, 0, null, List.of())
                            : nutrientsMapper.toResponse(entity);

                    List<ProductsPerDayEntity> dayPpds = ppdByDate.getOrDefault(date, List.of());
                    List<ProductsPerDayResponse> ppdResponses = dayPpds.stream()
                            .map(ppd -> {
                                ProductResponse product = productsById.get(ppd.getId().getProductId());
                                if (product == null) {
                                    throw new ProductNotFoundException(ppd.getId().getProductId());
                                }
                                return productsPerDayMapper.toResponse(product, ppd.getQuantity());
                            })
                            .sorted(Comparator.comparing(ProductsPerDayResponse::name))
                            .toList();

                    return new NutrientsResponse(
                            response.date(),
                            response.calories(),
                            response.protein(),
                            response.fat(),
                            response.carbohydrates(),
                            response.fiber(),
                            response.comment(),
                            ppdResponses
                    );
                })
                .toList();
    }

    public NutrientsExpenditureRs calculateDailyNutrients(NutrientsExpenditureRq request) {
        return nutritionCalculator.calculateDailyNutrients(request);
    }

    private double round(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}