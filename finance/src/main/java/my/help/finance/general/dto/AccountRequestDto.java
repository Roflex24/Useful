package my.help.finance.general.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import my.help.finance.general.entity.AccountType;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountRequestDto {
    @NotBlank(message = "Название банка обязательно")
    private String bankName;

    @NotNull(message = "Сумма обязательна")
    @DecimalMin(value = "0.00", message = "Сумма не может быть отрицательной")
    // Для INVESTMENT это поле игнорируется и пересчитывается автоматически
    // на основе бумаг (см. AccountService/SecurityService)
    private BigDecimal amount;

    @NotNull(message = "Тип счёта обязателен")
    private AccountType type;

    private String comment;

    // Для DEPOSIT и SAVINGS обязательно (для SAVINGS endDate можно не указывать), для CARD/INVESTMENT - null
    private DepositInfoDto depositInfoDto;
}