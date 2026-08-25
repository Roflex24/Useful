package my.help.finance.general.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import my.help.finance.general.entity.AccountType;

import java.math.BigDecimal;
import java.util.Map;

@Data
@AllArgsConstructor
@Builder
public class FinanceSummaryDto {
    private BigDecimal totalAmount;                    // Общая сумма всех средств
    private Map<String, BigDecimal> amountByBank;      // По банкам
    private Map<AccountType, BigDecimal> amountByType; // По типу (вклады/инвестиции)
    private Map<String, BankCashbackSummaryDto> cashbackSummaryByBank; // Сводка кешбека по банкам
    private Map<String, BigDecimal> bestCashbackByCategory;            // Лучший кешбек по категориям
}