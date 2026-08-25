package my.help.finance.general.dto;

import java.math.BigDecimal;

public record CashbackRs(
        Long id,
        Long accountId,
        String bankName,
        String category,
        BigDecimal percentage
) {}