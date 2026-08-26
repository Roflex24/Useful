package my.help.finance.general.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import my.help.finance.general.entity.AccountType;

import java.math.BigDecimal;

public record AccountRq(
        @NotBlank(message = "Название банка обязательно")
        String bankName,

        @NotNull(message = "Сумма обязательна")
        @DecimalMin(value = "0.00", message = "Сумма не может быть отрицательной")
        BigDecimal amount,

        @NotNull(message = "Тип счёта обязателен")
        AccountType type,

        String comment,

        DepositInfoDto depositInfoDto
) {
}