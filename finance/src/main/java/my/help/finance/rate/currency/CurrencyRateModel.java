package my.help.finance.rate.currency;

import java.time.LocalDate;

public record CurrencyRateModel (double usdRate, double eurRate, LocalDate date) {
}
