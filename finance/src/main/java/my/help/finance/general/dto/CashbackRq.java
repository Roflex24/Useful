package my.help.finance.general.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CashbackRq(
        @NotNull(message = "ID счёта обязателен")
        Long accountId,

        @NotBlank(message = "Категория обязательна")
        String category,

        @NotNull(message = "Процент кешбека обязателен")
        @DecimalMin(value = "0.01", message = "Процент должен быть больше 0")
        BigDecimal percentage
) {}