package my.help.finance.general.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class SecurityRequestDto {
    @NotNull(message = "ID счёта обязателен")
    private Long accountId;

    @NotNull(message = "Тип бумаги обязателен")
    private SecurityType securityType;

    @NotBlank(message = "Тикер/название обязательно")
    private String ticker;

    @NotNull(message = "Количество обязательно")
    @DecimalMin(value = "0.000001", message = "Количество должно быть больше 0")
    private BigDecimal quantity;

    @NotNull(message = "Текущая цена обязательна")
    @DecimalMin(value = "0.000001", message = "Цена должна быть больше 0")
    private BigDecimal currentPrice;

    // Только для BOND
    private BigDecimal faceValue;
    private BigDecimal couponRate;
    private LocalDate maturityDate;
}