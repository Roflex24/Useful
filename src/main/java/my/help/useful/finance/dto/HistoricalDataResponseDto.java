package my.help.useful.finance.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HistoricalDataResponseDto {
    private LocalDate snapshotDate;                    // За какой месяц данные
    private List<AccountResponseDto> accounts;         // Счета на эту дату
    private FinanceSummaryDto summary;                 // Сводка на эту дату
    private List<BankCashbackSummaryDto> cashbackSummary; // Кешбек сводка
}