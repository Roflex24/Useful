package my.help.finance.avito.dto;

import java.util.Map;

public record ApartmentStats(
        long count,
        Long minPrice,
        Long maxPrice,
        Long avgPrice,
        Long medianPrice,
        Long avgPricePerMeter,
        Map<String, Long> byRooms,
        Map<String, Long> byDistrict
) {}