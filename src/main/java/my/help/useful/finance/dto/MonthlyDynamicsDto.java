package my.help.useful.finance.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.YearMonth;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyDynamicsDto {
    private YearMonth month;                // Месяц (например 2026-01)
    private String monthLabel;              // Читаемый формат: "Январь 2026"
    private BigDecimal totalAmount;         // Общая сумма всех средств
    private BigDecimal cardAmount;          // Карты/счета (CARD)
    private BigDecimal depositAmount;       // Вклады (DEPOSIT)
    private BigDecimal investmentAmount;    // Инвестиции (INVESTMENT)
}