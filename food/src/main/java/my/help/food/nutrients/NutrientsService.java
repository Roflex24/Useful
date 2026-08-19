package my.help.food.nutrients;

import lombok.RequiredArgsConstructor;
import my.help.food.common.exception.NutrientsNotFoundException;
import my.help.food.nutrients.dto.*;
import my.help.food.product.ProductService;
import my.help.food.product.dto.ProductRs;
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
    public List<NutrientsRs> getList() {
        return nutrientsMapper.toResponseList(nutrientsRepository.findAll());
    }

    @Transactional
    public NutrientsRs updatePerDate(LocalDate date, NutrientsUpdateRq rq) {
        Map<Long, Double> quantities = toQuantityMap(rq.productsPerDay());
        Map<Long, ProductRs> productsById = productService.getProductsByIds(quantities.keySet());

        NutritionCalculator.NutritionTotals totals = nutritionCalculator.calculate(quantities, productsById);

        NutrientsPerDayEntity entity = new NutrientsPerDayEntity(
                date,
                totals.calories(),
                totals.protein(),
                totals.fat(),
                totals.carbohydrates(),
                totals.fiber(),
                rq.comment()
        );
        nutrientsRepository.save(entity);
        productsPerDayService.replacePerDay(date, quantities);

        return buildResponse(entity, quantities, productsById);
    }

    @Transactional(readOnly = true)
    public NutrientsRs getPerDate(LocalDate date) {
        NutrientsPerDayEntity entity = nutrientsRepository.findById(date)
                .orElseThrow(() -> new NutrientsNotFoundException("Данные за " + date + " не найдены"));

        List<ProductsPerDayEntity> ppdEntities = productsPerDayService.getPerDate(date);
        Map<Long, Double> quantities = toQuantityMapFromEntities(ppdEntities);
        Map<Long, ProductRs> productsById = productService.getProductsByIds(quantities.keySet());

        return buildResponse(entity, quantities, productsById);
    }

    @Transactional(readOnly = true)
    public List<NutrientsRs> getForWeek() {
        LocalDate today = LocalDate.now();
        LocalDate start = today.minusDays(6);

        List<NutrientsPerDayEntity> nutrientsEntities =
                nutrientsRepository.findByDateBetween(start, today);

        Map<LocalDate, NutrientsPerDayEntity> nutrientsByDate = nutrientsEntities.stream()
                .collect(Collectors.toMap(
                        NutrientsPerDayEntity::getDate,
                        Function.identity()
                ));

        List<ProductsPerDayEntity> ppdEntities =
                productsPerDayService.getBetween(start, today);

        // Уникальные ID продуктов за неделю
        Set<Long> productIds = ppdEntities.stream()
                .map(e -> e.getId().getProductId())
                .collect(Collectors.toSet());

        Map<Long, ProductRs> productsById = productIds.isEmpty()
                ? Map.of()
                : productService.getProductsByIds(productIds);

        // Группируем количества по датам
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
                    Map<Long, Double> dayQuantities =
                            quantitiesByDate.getOrDefault(date, Map.of());

                    if (entity == null) {
                        return new NutrientsRs(
                                date, 0, 0, 0, 0, 0, null, List.of()
                        );
                    }

                    return buildResponse(entity, dayQuantities, productsById);
                })
                .toList();
    }

    private Map<Long, Double> toQuantityMap(List<ProductsPerDayRq> items) {
        if (items == null || items.isEmpty()) {
            return Map.of();
        }
        Map<Long, Double> map = new HashMap<>();
        for (ProductsPerDayRq item : items) {
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

    private NutrientsRs buildResponse(NutrientsPerDayEntity entity,
                                      Map<Long, Double> quantities,
                                      Map<Long, ProductRs> productsById) {
        List<ProductsPerDayRs> items = quantities.entrySet().stream()
                .map(entry -> productsPerDayMapper.toResponse(
                        productsById.get(entry.getKey()),
                        entry.getValue()))
                .sorted(Comparator.comparing(ProductsPerDayRs::name))
                .toList();

        return new NutrientsRs(
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