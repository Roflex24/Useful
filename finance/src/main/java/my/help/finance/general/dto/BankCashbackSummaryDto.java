package my.help.finance.general.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record BankCashbackSummaryDto(
        String bankName,
        int totalCashbackCategories,
        BigDecimal bestCashbackPercentage,
        String bestCashbackCategory,
        Map<String, BigDecimal> cashbackByCategory,
        List<CashbackRs> activeCashbacks
) {
}