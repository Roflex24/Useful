package my.help.finance.general.dto;

import java.time.LocalDate;
import java.util.List;

public record HistoricalDataResponseDto(
        LocalDate snapshotDate,
        List<AccountRs> accounts,
        FinanceSummaryDto summary,
        List<BankCashbackSummaryDto> cashbackSummary
) {}