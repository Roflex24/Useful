package my.help.finance.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "deposits")
@Data
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