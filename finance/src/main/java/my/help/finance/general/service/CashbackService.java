package my.help.finance.general.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.help.finance.general.dto.BankCashbackSummaryDto;
import my.help.finance.general.dto.CashbackRq;
import my.help.finance.general.dto.CashbackRs;
import my.help.finance.general.entity.Account;
import my.help.finance.general.entity.AccountType;
import my.help.finance.general.entity.Cashback;
import my.help.finance.general.mapper.CashbackMapper;
import my.help.finance.general.repository.AccountRepository;
import my.help.finance.general.repository.CashbackRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CashbackService {

    private final CashbackRepository cashbackRepository;
    private final AccountRepository accountRepository;
    private final CashbackMapper cashbackMapper;

    public List<CashbackRs> getAllCashbacks() {
        log.debug("Fetching all cashbacks");
        return cashbackRepository.findAll()
                .stream()
                .map(cashbackMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    public List<CashbackRs> getCashbacksByAccount(Long accountId) {
        log.debug("Fetching cashbacks for account: {}", accountId);

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found with id: " + accountId));

        if (account.getType() != AccountType.CARD) {
            log.warn("Attempt to get cashbacks for non-card account: {} (type: {})", accountId, account.getType());
            return Collections.emptyList();
        }

        return cashbackRepository.findByAccountId(accountId)
                .stream()
                .map(cashbackMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public CashbackRs createCashback(CashbackRq rq) {
        log.info("Creating cashback for account: {}", rq.accountId());

        Account account = accountRepository.findById(rq.accountId())
                .orElseThrow(() -> new RuntimeException("Account not found with id: " + rq.accountId()));

        if (account.getType() != AccountType.CARD) {
            throw new RuntimeException("Cashback can only be added to CARD accounts. Current account type: " + account.getType());
        }

        Cashback cashback = cashbackMapper.toEntity(rq);
        Cashback savedCashback = cashbackRepository.save(cashback);
        log.info("Cashback created successfully with id: {}", savedCashback.getId());

        return cashbackMapper.toResponseDto(savedCashback);
    }

    @Transactional
    public CashbackRs updateCashback(Long id, CashbackRq rq) {
        log.info("Updating cashback with id: {}", id);

        Cashback existingCashback = cashbackRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cashback not found with id: " + id));

        Account account = existingCashback.getAccount();
        if (account.getType() != AccountType.CARD) {
            throw new RuntimeException("Cannot update cashback for non-card account");
        }

        existingCashback.setCategory(rq.category());
        existingCashback.setPercentage(rq.percentage());

        Cashback updatedCashback = cashbackRepository.save(existingCashback);
        log.info("Cashback updated successfully with id: {}", id);

        return cashbackMapper.toResponseDto(updatedCashback);
    }

    @Transactional
    public void deleteCashback(Long id) {
        log.info("Deleting cashback with id: {}", id);

        if (!cashbackRepository.existsById(id)) {
            throw new RuntimeException("Cashback not found with id: " + id);
        }

        cashbackRepository.deleteById(id);
        log.info("Cashback deleted successfully with id: {}", id);
    }

    public List<BankCashbackSummaryDto> getCashbackSummaryByBank() {
        log.debug("Calculating cashback summary by bank");

        List<Account> cardAccounts = accountRepository.findAll().stream()
                .filter(account -> account.getType() == AccountType.CARD)
                .toList();

        Map<String, List<CashbackRs>> cashbacksByBank = new HashMap<>();

        for (Account account : cardAccounts) {
            List<CashbackRs> accountCashbacks = getCashbacksByAccount(account.getId());
            if (!accountCashbacks.isEmpty()) {
                cashbacksByBank.put(account.getBankName(), accountCashbacks);
            }
        }

        List<BankCashbackSummaryDto> summary = new ArrayList<>();

        for (Map.Entry<String, List<CashbackRs>> entry : cashbacksByBank.entrySet()) {
            String bankName = entry.getKey();
            List<CashbackRs> cashbacks = entry.getValue();

            Map<String, BigDecimal> cashbackByCategory = cashbacks.stream()
                    .collect(Collectors.toMap(
                            CashbackRs::category,
                            CashbackRs::percentage
                    ));

            Optional<CashbackRs> bestCashback = cashbacks.stream()
                    .max(Comparator.comparing(CashbackRs::percentage));

            summary.add(new BankCashbackSummaryDto(
                    bankName,
                    cashbacks.size(),
                    bestCashback.map(CashbackRs::percentage).orElse(BigDecimal.ZERO),
                    bestCashback.map(CashbackRs::category).orElse("Нет"),
                    cashbackByCategory,
                    cashbacks
            ));
        }

        return summary;
    }

    public Map<String, BigDecimal> getBestCashbackForCategories() {
        log.debug("Getting best cashback offers for each category");

        List<Cashback> activeCashbacks = cashbackRepository.findAll().stream()
                .filter(cb -> cb.getAccount().getType() == AccountType.CARD)
                .toList();

        Map<String, BigDecimal> bestCashbackByCategory = new HashMap<>();

        for (Cashback cashback : activeCashbacks) {
            String category = cashback.getCategory();
            BigDecimal percentage = cashback.getPercentage();

            if (bestCashbackByCategory.containsKey(category)) {
                if (percentage.compareTo(bestCashbackByCategory.get(category)) > 0) {
                    bestCashbackByCategory.put(category, percentage);
                }
            } else {
                bestCashbackByCategory.put(category, percentage);
            }
        }

        return bestCashbackByCategory;
    }

    public List<Cashback> getCashbacksByAccountEntity(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        if (account.getType() != AccountType.CARD) {
            return Collections.emptyList();
        }

        return cashbackRepository.findByAccount(account);
    }
}