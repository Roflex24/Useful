package my.help.finance.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import my.help.finance.entity.SecurityType;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SecurityResponseDto {
    private Long id;
    private Long accountId;
    private String bankName;          // Название счёта/брокера (для единообразия с CashbackResponseDto)
    private SecurityType securityType;
    private String ticker;
    private BigDecimal quantity;
    private BigDecimal currentPrice;
    private BigDecimal totalValue;    // quantity * currentPrice

    // Только для BOND (для остальных типов — null)
    private BigDecimal faceValue;
    private BigDecimal couponRate;
    private LocalDate maturityDate;
}