package my.help.finance.avito.ai;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.help.finance.avito.dto.ApartmentBrief;
import my.help.finance.avito.dto.ApartmentStats;
import my.help.finance.avito.entity.Apartment;
import my.help.finance.avito.repository.ApartmentRepository;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class ApartmentAiTools {

    private static final int MAX_LIMIT = 200;

    private final ApartmentRepository repository;

    // ------------------------------------------------------------------
    //  ИНСТРУМЕНТ 1: количество с фильтром
    // ------------------------------------------------------------------

    @Tool(description = """
            Возвращает количество квартир в базе, удовлетворяющих фильтру.
            Все параметры опциональны. Используй этот метод, чтобы быстро
            ответить «сколько у меня квартир дешевле X», «сколько студий
            в районе Y» и т.п., не выгружая сами объявления.
            """)
    @Transactional(readOnly = true)
    public long countApartments(
            @ToolParam(description = "Минимум комнат", required = false) Integer minRooms,
            @ToolParam(description = "Максимум комнат", required = false) Integer maxRooms,
            @ToolParam(description = "Минимальная цена, ₽", required = false) Long minPrice,
            @ToolParam(description = "Максимальная цена, ₽", required = false) Long maxPrice,
            @ToolParam(description = "Минимальная общая площадь, м²", required = false) Double minArea,
            @ToolParam(description = "Максимальная общая площадь, м²", required = false) Double maxArea,
            @ToolParam(description = "Подстрока названия метро", required = false) String metro
    ) {
        long count = filter(minRooms, maxRooms, minPrice, maxPrice, minArea, maxArea, metro).count();
        log.info("countApartments result: count={}", count);
        return count;
    }

    // ------------------------------------------------------------------
    //  ИНСТРУМЕНТ 2: краткий список объявлений
    // ------------------------------------------------------------------

    @Tool(description = """
            Ищет квартиры в базе и возвращает краткие карточки (id, avitoId,
            цена, площадь, комнаты, метро, адрес, ремонт, url).
            Результат ограничен параметром limit (по умолчанию 30, max 200).
            """)
    @Transactional(readOnly = true)
    public List<ApartmentBrief> searchApartments(
            @ToolParam(description = "Минимум комнат", required = false) Integer minRooms,
            @ToolParam(description = "Максимум комнат", required = false) Integer maxRooms,
            @ToolParam(description = "Минимальная цена, ₽", required = false) Long minPrice,
            @ToolParam(description = "Максимальная цена, ₽", required = false) Long maxPrice,
            @ToolParam(description = "Минимальная общая площадь, м²", required = false) Double minArea,
            @ToolParam(description = "Максимальная общая площадь, м²", required = false) Double maxArea,
            @ToolParam(description = "Подстрока района", required = false) String district,
            @ToolParam(description = "Подстрока метро", required = false) String metro,
            @ToolParam(description = "Сколько вернуть (default 30, max 200)", required = false) Integer limit,
            @ToolParam(description = "Сортировка: 'price', 'pricePerMeter', 'area', 'freshness' (default).", required = false) String sort
    ) {
        int n = (limit == null || limit <= 0) ? 30 : Math.min(limit, MAX_LIMIT);

        Comparator<Apartment> cmp = switch (sort == null ? "freshness" : sort.toLowerCase()) {
            case "price"          -> Comparator.comparing(Apartment::getPrice, Comparator.nullsLast(Long::compareTo));
            case "pricepermeter"  -> Comparator.comparing(Apartment::getPricePerMeter, Comparator.nullsLast(Long::compareTo));
            case "area"           -> Comparator.comparing(Apartment::getTotalArea, Comparator.nullsLast(Double::compareTo)).reversed();
            default               -> Comparator.comparing(Apartment::getId, Comparator.nullsLast(Long::compareTo)).reversed();
        };

        List<ApartmentBrief> result = filter(minRooms, maxRooms, minPrice, maxPrice, minArea, maxArea, metro)
                .sorted(cmp)
                .limit(n)
                .map(ApartmentBrief::from)
                .toList();

        log.info("searchApartments result: size={}", result.size());
        return result;
    }

    // ------------------------------------------------------------------
    //  ИНСТРУМЕНТ 3: агрегированная статистика
    // ------------------------------------------------------------------

    @Tool(description = """
            Считает агрегированную статистику по подмножеству квартир:
            количество, мин/макс/средняя/медианная цена, средняя цена за м²,
            распределение по числу комнат и по районам.
            """)
    @Transactional(readOnly = true)
    public ApartmentStats getStats(
            @ToolParam(description = "Минимум комнат", required = false) Integer minRooms,
            @ToolParam(description = "Максимум комнат", required = false) Integer maxRooms,
            @ToolParam(description = "Минимальная цена, ₽", required = false) Long minPrice,
            @ToolParam(description = "Максимальная цена, ₽", required = false) Long maxPrice,
            @ToolParam(description = "Подстрока района", required = false) String district,
            @ToolParam(description = "Подстрока метро", required = false) String metro
    ) {
        List<Apartment> list = filter(minRooms, maxRooms, minPrice, maxPrice,
                null, null, metro).toList();

        List<Long> prices = list.stream().map(Apartment::getPrice).filter(Objects::nonNull).sorted().toList();
        List<Long> ppm    = list.stream().map(Apartment::getPricePerMeter).filter(Objects::nonNull).sorted().toList();

        Long min = prices.isEmpty() ? null : prices.getFirst();
        Long max = prices.isEmpty() ? null : prices.getLast();
        Long avg = prices.isEmpty() ? null : Math.round(prices.stream().mapToLong(Long::longValue).average().orElse(0));
        Long med = prices.isEmpty() ? null : prices.get(prices.size() / 2);
        Long avgPpm = ppm.isEmpty() ? null : Math.round(ppm.stream().mapToLong(Long::longValue).average().orElse(0));

        Map<String, Long> byRooms = list.stream()
                .filter(a -> a.getRooms() != null)
                .collect(Collectors.groupingBy(a -> a.getRooms() + "-комн.", LinkedHashMap::new, Collectors.counting()));

        return new ApartmentStats(list.size(), min, max, avg, med, avgPpm, byRooms);
    }

    // ------------------------------------------------------------------

    private java.util.stream.Stream<Apartment> filter(
            Integer minRooms, Integer maxRooms,
            Long minPrice, Long maxPrice,
            Double minArea, Double maxArea,
            String metro
    ) {
        Predicate<Apartment> p = a -> true;
        if (minRooms != null) p = p.and(a -> a.getRooms() != null && a.getRooms() >= minRooms);
        if (maxRooms != null) p = p.and(a -> a.getRooms() != null && a.getRooms() <= maxRooms);
        if (minPrice != null) p = p.and(a -> a.getPrice() != null && a.getPrice() >= minPrice);
        if (maxPrice != null) p = p.and(a -> a.getPrice() != null && a.getPrice() <= maxPrice);
        if (minArea  != null) p = p.and(a -> a.getTotalArea() != null && a.getTotalArea() >= minArea);
        if (maxArea  != null) p = p.and(a -> a.getTotalArea() != null && a.getTotalArea() <= maxArea);
        if (metro != null && !metro.isBlank()) {
            String m = metro.toLowerCase();
            p = p.and(a -> a.getMetroName() != null && a.getMetroName().toLowerCase().contains(m));
        }
        return repository.findAll().stream().filter(p);
    }
}