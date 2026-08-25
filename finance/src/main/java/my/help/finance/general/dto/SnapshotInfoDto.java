package my.help.finance.general.dto;

import java.time.LocalDate;
import java.time.YearMonth;

public record SnapshotInfoDto(
        YearMonth yearMonth,
        LocalDate snapshotDate,
        int accountsCount,
        int cashbacksCount,
        String formattedDate // "Январь 2026"
) {
    public int getYear() {
        return yearMonth != null ? yearMonth.getYear() : 0;
    }

    public int getMonth() {
        return yearMonth != null ? yearMonth.getMonthValue() : 0;
    }
}