package my.help.food.nutrients;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.help.food.common.exception.NutrientsNotFoundException;
import my.help.food.nutrients.dto.*;
import my.help.food.product.ProductValidator;
import my.help.food.products_per_day.ProductsPerDayService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class NutrientsService {

    private final NutrientsRepository nutrientsRepository;
    private final NutrientsMapper nutrientsMapper;
    private final ProductsPerDayService productsPerDayService;
    private final ProductValidator productValidator;

    @Transactional(readOnly = true)
    public Page<NutrientsRs> getPage(Pageable pageable) {
        log.debug("Получение всех записей питания");
        Page<NutrientsPerDayEntity> page = nutrientsRepository.findAll(
                PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort())
        );
        log.info("Найдено записей: {} (всего: {})", page.getNumberOfElements(), page.getTotalElements());
        return page.map(nutrientsMapper::toResponse);
    }

    @Transactional
    public NutrientsRs updatePerDate(LocalDate date, NutrientsUpdateRq rq) {
        log.info("Обновление данных за дату {}", date);
        List<ProductsPerDayRq> products = rq.productsPerDay() != null ? rq.productsPerDay() : List.of();
        validateProducts(products);

        Map<Long, Double> quantities = toQuantityMap(products);

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
        log.debug("Сохранена сводка за дату {}", date);
        productsPerDayService.replacePerDate(date, quantities);
        log.info("Данные за дату {} успешно обновлены", date);

        return getPerDate(date);
    }

    @Transactional(readOnly = true)
    public NutrientsRs getPerDate(LocalDate date) {
        log.debug("Запрос данных за дату {}", date);
        List<NutrientsWithProductsRowProjection> rows =
                nutrientsRepository.findWithProductsByDate(date);

        if (rows.isEmpty()) {
            log.warn("Данные за {} не найдены", date);
            throw new NutrientsNotFoundException("Данные за " + date + " не найдены");
        }

        NutrientsRs result = buildNutrientsRs(rows);
        log.info("За дату {} получено продуктов: {}", date, result.productsPerDay().size());
        return result;
    }

    @Transactional(readOnly = true)
    public List<NutrientsRs> getForWeek() {
        LocalDate today = LocalDate.now();
        LocalDate start = today.minusDays(6);
        log.info("Получение данных за неделю с {} по {}", start, today);

        List<NutrientsWithProductsRowProjection> rows =
                nutrientsRepository.findWithProductsByDateBetween(start, today);

        Map<LocalDate, List<NutrientsWithProductsRowProjection>> rowsByDate = rows.stream()
                .collect(Collectors.groupingBy(NutrientsWithProductsRowProjection::date));

        List<NutrientsRs> result = Stream.iterate(start, d -> d.plusDays(1))
                .limit(7)
                .map(date -> {
                    List<NutrientsWithProductsRowProjection> dayRows =
                            rowsByDate.getOrDefault(date, List.of());

                    if (dayRows.isEmpty()) {
                        log.debug("День {} отсутствует в БД, возвращаем пустой рацион", date);
                        return emptyNutrientsRs(date);
                    }
                    return buildNutrientsRs(dayRows);
                })
                .toList();
        log.info("Сформирована недельная выборка, дней: {}", result.size());
        return result;
    }

    private void validateProducts(List<ProductsPerDayRq> items) {
        List<Long> productIds = items.stream().map(ProductsPerDayRq::productId).toList();
        productValidator.validateNoDuplicateProductIds(productIds);
        productValidator.validateExist(productIds);
    }

    private NutrientsRs buildNutrientsRs(List<NutrientsWithProductsRowProjection> rows) {
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

    private NutrientsRs emptyNutrientsRs(LocalDate date) {
        return new NutrientsRs(date, 0, 0, 0, 0, 0, null, List.of());
    }

    private Map<Long, Double> toQuantityMap(List<ProductsPerDayRq> items) {
        if (items.isEmpty()) {
            log.debug("Список продуктов пуст");
            return Map.of();
        }
        Map<Long, Double> map = new HashMap<>();
        for (ProductsPerDayRq item : items) {
            if (map.putIfAbsent(item.productId(), item.quantity()) != null) {
                log.warn("Обнаружен дубликат productId: {}", item.productId());
                throw new IllegalArgumentException("Дубликат productId: " + item.productId());
            }
        }
        log.debug("Построена карта продуктов: {}", map.size());
        return map;
    }
}
