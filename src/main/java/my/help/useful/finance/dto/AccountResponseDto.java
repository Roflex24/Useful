package my.help.useful.finance.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import my.help.useful.finance.entity.AccountType;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountResponseDto {
    private Long id;
    private String bankName;
    private BigDecimal amount;
    private AccountType type;
    private String comment;

    private List<CashbackResponseDto> cashbacks;
}