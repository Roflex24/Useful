package my.help.finance.general.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import my.help.finance.general.entity.SecurityType;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SecuritySnapshotDto {
    private Long id;
    private SecurityType securityType;
    private String ticker;
    private BigDecimal quantity;
    private BigDecimal currentPrice;
    private BigDecimal faceValue;
    private BigDecimal couponRate;
    private LocalDate maturityDate;
}