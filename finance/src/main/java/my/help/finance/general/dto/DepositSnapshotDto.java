package my.help.finance.general.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepositSnapshotDto {
    private Long id;
    private LocalDate endDate;
    private LocalDate interestPaymentDate;
    private BigDecimal interestRate;
}