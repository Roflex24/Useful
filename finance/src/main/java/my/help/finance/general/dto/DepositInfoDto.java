package my.help.finance.general.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DepositInfoDto(
        Long id,
        LocalDate endDate,
        LocalDate interestPaymentDate,
        BigDecimal interestRate
) {}