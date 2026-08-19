package my.help.food.nutrients;

import lombok.RequiredArgsConstructor;
import my.help.food.common.exception.NutrientsNotFoundException;
import my.help.food.nutrients.dto.*;
import my.help.food.products_per_day.ProductsPerDayService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class NutrientsService {

    private final NutrientsRepository nutrientsRepository;
    private final NutrientsMapper nutrientsMapper;
    private final ProductsPerDayService productsPerDayService;

    @Transactional(readOnly = true)
    public List<NutrientsRs> getList() {
        return nutrientsMapper.toResponseList(nutrientsRepository.findAll());
    }

    @Transactional
    public NutrientsRs updatePerDate(LocalDate date, NutrientsUpdateRq rq) {
        Map<Long, Double> quantities = toQuantityMap(rq.productsPerDay());

        NutrientsPerDayEntity entity = new NutrientsPerDayEntity(
                date,
                rq.calories(),
                rq.protein(),
                rq.fat(),
                rq.carbohydrates(),
                rq.fiber(),
                rq.comment()
        );
        nutrientsRepository.save(entity);
        productsPerDayService.replacePerDate(date, quantities);

        return getPerDate(date);
    }

    @Transactional(readOnly = true)
    public NutrientsRs getPerDate(LocalDate date) {
        List<NutrientsWithProductsRowProjection> rows =
                nutrientsRepository.findWithProductsByDate(date);

        if (rows.isEmpty()) {
            throw new NutrientsNotFoundException("Данные за " + date + " не найдены");
        }

        NutrientsWithProductsRowProjection first = rows.getFirst();
        List<ProductsPerDayRs> products = rows.stream()
                .filter(row -> row.productId() != null)
                .map(nutrientsMapper::toProductsPerDayRs)
                .sorted(Comparator.comparing(ProductsPerDayRs::name))
                .toList();

        return new NutrientsRs(
                first.date(),
                first.calories(),
                first.protein(),
                first.fat(),
                first.carbohydrates(),
                first.fiber(),
                first.comment(),
                products
        );
    }

    @Transactional(readOnly = true)
    public List<NutrientsRs> getForWeek() {
        LocalDate today = LocalDate.now();
        LocalDate start = today.minusDays(6);

        List<NutrientsWithProductsRowProjection> rows =
                nutrientsRepository.findWithProductsByDateBetween(start, today);

        Map<LocalDate, List<NutrientsWithProductsRowProjection>> rowsByDate = rows.stream()
                .collect(Collectors.groupingBy(NutrientsWithProductsRowProjection::date));

        return Stream.iterate(start, d -> d.plusDays(1))
                .limit(7)
                .map(date -> {
                    List<NutrientsWithProductsRowProjection> dayRows =
                            rowsByDate.getOrDefault(date, List.of());

                    if (dayRows.isEmpty()) {
                        return new NutrientsRs(
                                date, 0, 0, 0, 0, 0, null, List.of()
                        );
                    }

                    NutrientsWithProductsRowProjection first = dayRows.getFirst();
                    List<ProductsPerDayRs> products = dayRows.stream()
                            .filter(row -> row.productId() != null)
                            .map(nutrientsMapper::toProductsPerDayRs)
                            .sorted(Comparator.comparing(ProductsPerDayRs::name))
                            .toList();

                    return new NutrientsRs(
                            first.date(),
                            first.calories(),
                            first.protein(),
                            first.fat(),
                            first.carbohydrates(),
                            first.fiber(),
                            first.comment(),
                            products
                    );
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
}