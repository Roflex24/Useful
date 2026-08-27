package my.help.finance.rate.currency;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "currency_rates")
public class CurrencyRate {

    @Id
    private LocalDate actualDate;

    private double usdRate;
    private double eurRate;

}
