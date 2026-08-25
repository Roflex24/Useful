package my.help.finance.general.dto;

import java.math.BigDecimal;

public record CashbackSnapshotDto(
        Long id,
        String category,
        BigDecimal percentage
) {}