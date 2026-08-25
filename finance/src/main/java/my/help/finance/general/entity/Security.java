package my.help.finance.general.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "securities")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Security {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;                  // Счёт INVESTMENT, к которому относится бумага

    @Enumerated(EnumType.STRING)
    private SecurityType securityType;        // STOCK, BOND, ETF, CURRENCY_METAL

    private String ticker;                    // Тикер или название бумаги (например "SBER", "Золото")
    private BigDecimal quantity;               // Количество
    private BigDecimal currentPrice;           // Текущая цена за единицу

    // ── Только для BOND (для остальных типов — null) ──
    private BigDecimal faceValue;              // Номинал облигации
    private BigDecimal couponRate;             // Купонная ставка (%)
    private LocalDate maturityDate;            // Дата погашения

    @Transient
    public BigDecimal getTotalValue() {
        if (quantity == null || currentPrice == null) return BigDecimal.ZERO;
        return quantity.multiply(currentPrice);
    }
}