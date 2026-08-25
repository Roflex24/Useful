package my.help.finance.general.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BankCashbackSummaryDto {
    private String bankName;
    private int totalCashbackCategories;
    private BigDecimal bestCashbackPercentage;
    private String bestCashbackCategory;
    private Map<String, BigDecimal> cashbackByCategory;
    private List<CashbackResponseDto> activeCashbacks;
}