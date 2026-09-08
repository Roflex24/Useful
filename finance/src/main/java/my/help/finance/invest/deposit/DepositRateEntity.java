package my.help.finance.invest.deposit;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Setter
@Getter
@Entity
@Table(name = "deposit_rates")
@NoArgsConstructor
@AllArgsConstructor
public class DepositRateEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "bank_name", nullable = false)
    private String bankName;

    @Column(name = "rate_min", nullable = false)
    private Double rateMin;

    @Column(name = "percent_calculation")
    private String percentCalculation;

    @Column(name = "parse_date", nullable = false)
    private LocalDate parseDate;
}