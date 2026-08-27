package my.help.finance.exchange_rate;

import java.time.LocalDate;

public record CurrencyRateModel (double usdRate, double eurRate, LocalDate date) {
}
