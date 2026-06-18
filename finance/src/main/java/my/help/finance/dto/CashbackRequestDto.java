package my.help.finance.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CashbackRequestDto {
    @NotNull(message = "ID счёта обязателен")
    private Long accountId;

    @NotBlank(message = "Категория обязательна")
    private String category;

    @NotNull(message = "Процент кешбека обязателен")
    @DecimalMin(value = "0.01", message = "Процент должен быть больше 0")
    private BigDecimal percentage;
}