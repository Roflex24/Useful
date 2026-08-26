package my.help.finance.general.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.help.finance.general.dto.BankCashbackSummaryDto;
import my.help.finance.general.dto.FinanceSummaryDto;
import my.help.finance.general.entity.AccountType;
import my.help.finance.general.repository.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FinanceSummaryService {

    private final AccountRepository accountRepository;
    private final CashbackService cashbackService;

    public FinanceSummaryDto getFinanceSummary() {
        log.debug("Calculating finance summary");

        BigDecimal totalAmount = accountRepository.getTotalAmount();
        Map<String, BigDecimal> amountByBank = calculateAmountByBank();
        Map<AccountType, BigDecimal> amountByType = calculateAmountByType();

        List<BankCashbackSummaryDto> cashbackSummaries = cashbackService.getCashbackSummaryByBank();
        Map<String, BankCashbackSummaryDto> cashbackSummaryByBank = cashbackSummaries.stream()
                .collect(Collectors.toMap(
                        BankCashbackSummaryDto::bankName,
                        summary -> summary
                ));

        Map<String, BigDecimal> bestCashbackByCategory = cashbackService.getBestCashbackForCategories();

        return FinanceSummaryDto.builder()
                .totalAmount(totalAmount)
                .amountByBank(amountByBank)
                .amountByType(amountByType)
                .cashbackSummaryByBank(cashbackSummaryByBank)
                .bestCashbackByCategory(bestCashbackByCategory)
                .build();
    }

    private Map<String, BigDecimal> calculateAmountByBank() {
        Map<String, BigDecimal> result = new HashMap<>();
        List<Object[]> bankData = accountRepository.getSumByBank();

        for (Object[] row : bankData) {
            String bankName = (String) row[0];
            BigDecimal amount = (BigDecimal) row[1];
            result.put(bankName, amount);
        }

        log.debug("Calculated amounts for {} banks", result.size());
        return result;
    }

    private Map<AccountType, BigDecimal> calculateAmountByType() {
        Map<AccountType, BigDecimal> result = new HashMap<>();
        List<Object[]> typeData = accountRepository.getSumByType();

        for (Object[] row : typeData) {
            AccountType type = (AccountType) row[0];
            BigDecimal amount = (BigDecimal) row[1];
            result.put(type, amount);
        }

        for (AccountType type : AccountType.values()) {
            result.putIfAbsent(type, BigDecimal.ZERO);
        }

        log.debug("Calculated amounts for {} types", result.size());
        return result;
    }
}