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
    private BigDecimal totalAmount;
    private Map<String, BigDecimal> amountByBank;
    private Map<AccountType, BigDecimal> amountByType;
    private Map<String, BankCashbackSummaryDto> cashbackSummaryByBank;
    private Map<String, BigDecimal> bestCashbackByCategory;
}