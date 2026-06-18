package my.help.finance.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

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
}