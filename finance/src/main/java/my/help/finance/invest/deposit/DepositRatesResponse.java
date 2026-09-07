package my.help.finance.invest.deposit;

import java.util.List;

public record DepositRatesResponse(
        int totalBanks,
        List<DepositRateDto> rates
) {}
