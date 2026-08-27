package my.help.finance.rate.key;

import java.time.LocalDate;

public record KeyRateRs(
        double keyRate,
        LocalDate date) {
}
