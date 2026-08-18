package my.help.food.nutrients;

import lombok.RequiredArgsConstructor;
import my.help.food.common.exception.NutrientsNotFoundException;
import my.help.food.nutrients.dto.*;
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

    @Transactional(readOnly = true)
    public List<NutrientsResponse> getNutrientsList() {
        return nutrientsMapper.toResponseList(nutrientsRepository.findAll());
    }

    @Transactional
    public NutrientsResponse updateNutrientsPerDate(LocalDate date, NutrientsUpdateRequest request) {
        Map<Long, Double> quantities = toQuantityMap(request.productsPerDay());
        Map<Long, ProductResponse> productsById = productService.getProductsByIds(quantities.keySet());

        NutritionCalculator.NutritionTotals totals = nutritionCalculator.calculate(quantities, productsById);

        NutrientsPerDayEntity entity = new NutrientsPerDayEntity(
                date,
                totals.calories(),
                totals.protein(),
                totals.fat(),
                totals.carbohydrates(),
                totals.fiber(),
                request.comment()
        );
        nutrientsRepository.save(entity);
        productsPerDayService.replaceProductsPerDay(date, quantities);

        return buildResponse(entity, quantities, productsById);
    }

    @Transactional(readOnly = true)
    public NutrientsResponse getNutrientsPerDate(LocalDate date) {
        NutrientsPerDayEntity entity = nutrientsRepository.findById(date)
                .orElseThrow(() -> new NutrientsNotFoundException("Данные за " + date + " не найдены"));

        List<ProductsPerDayEntity> ppdEntities = productsPerDayService.getProductsPerDate(date);
        Map<Long, Double> quantities = toQuantityMapFromEntities(ppdEntities);
        Map<Long, ProductResponse> productsById = productService.getProductsByIds(quantities.keySet());

        return buildResponse(entity, quantities, productsById);
    }

    @Transactional(readOnly = true)
    public List<NutrientsResponse> getNutrientsPerDateForWeek() {
        LocalDate today = LocalDate.now();
        LocalDate start = today.minusDays(6);

        List<NutrientsPerDayEntity> nutrientsEntities = nutrientsRepository.findByDateBetween(start, today);
        Map<LocalDate, NutrientsPerDayEntity> nutrientsByDate = nutrientsEntities.stream()
                .collect(Collectors.toMap(NutrientsPerDayEntity::getDate, Function.identity()));

        List<ProductsPerDayEntity> ppdEntities = productsPerDayService.getProductsBetween(start, today);
        Map<Long, Double> quantities = toQuantityMapFromEntities(ppdEntities);
        Map<Long, ProductResponse> productsById = productService.getProductsByIds(quantities.keySet());

        Map<LocalDate, Map<Long, Double>> quantitiesByDate = ppdEntities.stream()
                .collect(Collectors.groupingBy(
                        ppd -> ppd.getId().getDate(),
                        Collectors.toMap(
                                ppd -> ppd.getId().getProductId(),
                                ProductsPerDayEntity::getQuantity
                        )
                ));

        return Stream.iterate(start, d -> d.plusDays(1))
                .limit(7)
                .map(date -> {
                    NutrientsPerDayEntity entity = nutrientsByDate.get(date);
                    NutrientsResponse base = entity == null
                            ? new NutrientsResponse(date, 0, 0, 0, 0, 0, null, List.of())
                            : nutrientsMapper.toResponse(entity);

                    Map<Long, Double> dayQuantities = quantitiesByDate.getOrDefault(date, Map.of());
                    return buildResponse(
                            entity != null ? entity : new NutrientsPerDayEntity(date, 0, 0, 0, 0, 0, null),
                            dayQuantities,
                            productsById
                    );
                })
                .toList();
    }

    private Map<Long, Double> toQuantityMap(List<ProductsPerDayRequest> items) {
        if (items == null || items.isEmpty()) {
            return Map.of();
        }
        Map<Long, Double> map = new HashMap<>();
        for (ProductsPerDayRequest item : items) {
            if (map.putIfAbsent(item.productId(), item.quantity()) != null) {
                throw new IllegalArgumentException("Дубликат productId: " + item.productId());
            }
        }
        return map;
    }

    private Map<Long, Double> toQuantityMapFromEntities(List<ProductsPerDayEntity> entities) {
        return entities.stream()
                .collect(Collectors.toMap(
                        e -> e.getId().getProductId(),
                        ProductsPerDayEntity::getQuantity
                ));
    }

    private NutrientsResponse buildResponse(NutrientsPerDayEntity entity,
                                            Map<Long, Double> quantities,
                                            Map<Long, ProductResponse> productsById) {
        List<ProductsPerDayResponse> items = quantities.entrySet().stream()
                .map(entry -> productsPerDayMapper.toResponse(
                        productsById.get(entry.getKey()),
                        entry.getValue()))
                .sorted(Comparator.comparing(ProductsPerDayResponse::name))
                .toList();

        return new NutrientsResponse(
                entity.getDate(),
                entity.getCalories(),
                entity.getProtein(),
                entity.getFat(),
                entity.getCarbohydrates(),
                entity.getFiber(),
                entity.getComment(),
                items
        );
    }
}