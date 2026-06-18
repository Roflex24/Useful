package my.help.useful.exchange_rate;

import java.time.LocalDate;

public record CurrencyRateModel (double usdRate, double eurRate, LocalDate date) {
}
