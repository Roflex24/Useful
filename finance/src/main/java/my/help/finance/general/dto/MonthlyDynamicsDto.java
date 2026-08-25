package my.help.finance.general.dto;

import java.math.BigDecimal;
import java.time.YearMonth;

public record MonthlyDynamicsDto(
        YearMonth month,                // Месяц (например 2026-01)
        String monthLabel,              // Читаемый формат: "Январь 2026"
        BigDecimal totalAmount,         // Общая сумма всех средств
        BigDecimal cardAmount,          // Карты/счета (CARD)
        BigDecimal depositAmount,       // Вклады (DEPOSIT)
        BigDecimal savingsAmount,       // Накопительные счета (SAVINGS)
        BigDecimal investmentAmount    // Инвестиции (INVESTMENT)
) {}