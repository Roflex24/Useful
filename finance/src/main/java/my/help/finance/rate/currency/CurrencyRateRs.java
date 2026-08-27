package my.help.finance.rate.currency;

import java.time.LocalDate;

public record CurrencyRateRs(
        double usdRate,
        double eurRate,
        LocalDate date) {
}
