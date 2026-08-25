package my.help.finance.general.dto;

import java.time.LocalDate;
import java.util.List;

public record HistoricalDataResponseDto(
        LocalDate snapshotDate,                    // За какой месяц данные
        List<AccountRs> accounts,         // Счета на эту дату
        FinanceSummaryDto summary,                 // Сводка на эту дату
        List<BankCashbackSummaryDto> cashbackSummary // Кешбек сводка
) {}