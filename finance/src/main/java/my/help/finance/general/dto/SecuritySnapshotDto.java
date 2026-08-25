package my.help.finance.general.dto;

import my.help.finance.general.entity.SecurityType;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SecuritySnapshotDto(
        Long id,
        SecurityType securityType,
        String ticker,
        BigDecimal quantity,
        BigDecimal currentPrice,
        BigDecimal faceValue,
        BigDecimal couponRate,
        LocalDate maturityDate
) {}