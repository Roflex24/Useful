package my.help.finance.invest.rate.currency;

import java.time.LocalDate;

public record CurrencyRateRs(
        double usdRate,
        double eurRate,
        LocalDate date) {
}
