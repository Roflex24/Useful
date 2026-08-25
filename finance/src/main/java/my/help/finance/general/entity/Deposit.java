package my.help.finance.general.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "deposits")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Deposit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false, unique = true)
    private Account account;

    private LocalDate endDate;              // Дата закрытия вклада (null для SAVINGS — без фиксированного срока)
    private LocalDate interestPaymentDate;  // Дата выплаты процентов
    private BigDecimal interestRate;        // Текущая процентная ставка
}