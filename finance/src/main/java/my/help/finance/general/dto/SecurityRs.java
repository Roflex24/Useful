package my.help.finance.general.dto;

import my.help.finance.general.entity.SecurityType;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SecurityRs(
        Long id,
        Long accountId,
        String bankName,
        SecurityType securityType,
        String ticker,
        BigDecimal quantity,
        BigDecimal currentPrice,
        BigDecimal totalValue,

        BigDecimal faceValue,
        BigDecimal couponRate,
        LocalDate maturityDate
) {}