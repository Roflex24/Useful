package my.help.useful.finance.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CashbackResponseDto {
    private Long id;
    private Long accountId;
    private String bankName;
    private String category;
    private BigDecimal percentage;
}