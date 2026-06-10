package my.help.useful.finance.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "cashbacks")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Cashback {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    private String category;          // Категория кешбека (например "Супермаркеты", "Такси")
    private BigDecimal percentage;    // Процент кешбека (например 5.0)
    private BigDecimal maxAmount;     // Максимальная сумма кешбека в месяц (опционально)
    private LocalDate validFrom;      // Дата начала действия
    private LocalDate validTo;        // Дата окончания действия
    private String description;       // Дополнительное описание
}