package my.help.finance.general.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import my.help.finance.general.entity.SecurityType;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SecurityRq(
        @NotNull(message = "ID счёта обязателен")
        Long accountId,

        @NotNull(message = "Тип бумаги обязателен")
        SecurityType securityType,

        @NotBlank(message = "Тикер/название обязательно")
        String ticker,

        @NotNull(message = "Количество обязательно")
        @DecimalMin(value = "0.000001", message = "Количество должно быть больше 0")
        BigDecimal quantity,

        @NotNull(message = "Текущая цена обязательна")
        @DecimalMin(value = "0.000001", message = "Цена должна быть больше 0")
        BigDecimal currentPrice,

        BigDecimal faceValue,
        BigDecimal couponRate,
        LocalDate maturityDate
) {}