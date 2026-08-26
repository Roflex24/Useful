package my.help.finance.general.dto;

import java.math.BigDecimal;
import java.time.YearMonth;

public record MonthlyDynamicsDto(
        YearMonth month,
        String monthLabel,
        BigDecimal totalAmount,
        BigDecimal cardAmount,
        BigDecimal depositAmount,
        BigDecimal savingsAmount,
        BigDecimal investmentAmount
) {}