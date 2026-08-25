package my.help.finance.avito.dto;

import my.help.finance.avito.entity.Apartment;

import java.util.List;

public record ParseRs(
        int parsedCount,
        List<Apartment> apartments,
        String error
) {}