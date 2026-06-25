package my.help.finance.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "accounts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String bankName;          // Например: "Сбербанк", "Тинькофф"
    private BigDecimal amount;        // Сумма
    private String comment;           // Доп. описание

    @Enumerated(EnumType.STRING)
    private AccountType type;         // CARD, DEPOSIT, SAVINGS или INVESTMENT
}