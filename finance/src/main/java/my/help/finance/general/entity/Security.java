package my.help.finance.general.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "securities")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Security {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Enumerated(EnumType.STRING)
    private SecurityType securityType;

    private String ticker;
    private BigDecimal quantity;
    private BigDecimal currentPrice;

    private BigDecimal faceValue;
    private BigDecimal couponRate;
    private LocalDate maturityDate;

    @Transient
    public BigDecimal getTotalValue() {
        if (quantity == null || currentPrice == null) return BigDecimal.ZERO;
        return quantity.multiply(currentPrice);
    }
}