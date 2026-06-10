package my.help.useful.finance.dto;

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
public class CashbackSnapshotDto {
    private Long id;
    private String category;
    private BigDecimal percentage;
    private BigDecimal maxAmount;
    private LocalDate validFrom;
    private LocalDate validTo;
    private String description;
}