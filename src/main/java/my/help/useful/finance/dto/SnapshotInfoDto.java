package my.help.useful.finance.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.YearMonth;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SnapshotInfoDto {
    private YearMonth yearMonth;
    private LocalDate snapshotDate;
    private int accountsCount;
    private int cashbacksCount;
    private String formattedDate; // "Январь 2026"

    public int getYear() {
        return yearMonth != null ? yearMonth.getYear() : 0;
    }

    public int getMonth() {
        return yearMonth != null ? yearMonth.getMonthValue() : 0;
    }
}