package my.help.finance.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import my.help.finance.entity.AccountType;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountRequestDto {
    @NotBlank(message = "Название банка обязательно")
    private String bankName;

    @NotNull(message = "Сумма обязательна")
    @DecimalMin(value = "0.01", message = "Сумма должна быть больше 0")
    private BigDecimal amount;

    @NotNull(message = "Тип счёта обязателен")
    private AccountType type;

    private String comment;
}