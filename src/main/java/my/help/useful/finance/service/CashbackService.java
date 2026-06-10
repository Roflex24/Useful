package my.help.useful.finance.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.help.useful.finance.dto.BankCashbackSummaryDto;
import my.help.useful.finance.dto.CashbackRequestDto;
import my.help.useful.finance.dto.CashbackResponseDto;
import my.help.useful.finance.entity.Account;
import my.help.useful.finance.entity.AccountType;
import my.help.useful.finance.entity.Cashback;
import my.help.useful.finance.mapper.CashbackMapper;
import my.help.useful.finance.repository.AccountRepository;
import my.help.useful.finance.repository.CashbackRepository;
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

    public List<CashbackResponseDto> getAllCashbacks() {
        log.debug("Fetching all cashbacks");
        return cashbackRepository.findAll()
                .stream()
                .map(cashbackMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    public List<CashbackResponseDto> getCashbacksByAccount(Long accountId) {
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
    public CashbackResponseDto createCashback(CashbackRequestDto requestDto) {
        log.info("Creating cashback for account: {}", requestDto.getAccountId());

        Account account = accountRepository.findById(requestDto.getAccountId())
                .orElseThrow(() -> new RuntimeException("Account not found with id: " + requestDto.getAccountId()));

        if (account.getType() != AccountType.CARD) {
            throw new RuntimeException("Cashback can only be added to CARD accounts. Current account type: " + account.getType());
        }

        Cashback cashback = cashbackMapper.toEntity(requestDto);
        Cashback savedCashback = cashbackRepository.save(cashback);
        log.info("Cashback created successfully with id: {}", savedCashback.getId());

        return cashbackMapper.toResponseDto(savedCashback);
    }

    @Transactional
    public CashbackResponseDto updateCashback(Long id, CashbackRequestDto requestDto) {
        log.info("Updating cashback with id: {}", id);

        Cashback existingCashback = cashbackRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cashback not found with id: " + id));

        Account account = existingCashback.getAccount();
        if (account.getType() != AccountType.CARD) {
            throw new RuntimeException("Cannot update cashback for non-card account");
        }

        existingCashback.setCategory(requestDto.getCategory());
        existingCashback.setPercentage(requestDto.getPercentage());

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

        Map<String, List<CashbackResponseDto>> cashbacksByBank = new HashMap<>();

        for (Account account : cardAccounts) {
            List<CashbackResponseDto> accountCashbacks = getCashbacksByAccount(account.getId());
            if (!accountCashbacks.isEmpty()) {
                cashbacksByBank.put(account.getBankName(), accountCashbacks);
            }
        }

        List<BankCashbackSummaryDto> summary = new ArrayList<>();

        for (Map.Entry<String, List<CashbackResponseDto>> entry : cashbacksByBank.entrySet()) {
            String bankName = entry.getKey();
            List<CashbackResponseDto> cashbacks = entry.getValue();

            Map<String, BigDecimal> cashbackByCategory = cashbacks.stream()
                    .collect(Collectors.toMap(
                            CashbackResponseDto::getCategory,
                            CashbackResponseDto::getPercentage
                    ));

            Optional<CashbackResponseDto> bestCashback = cashbacks.stream()
                    .max(Comparator.comparing(CashbackResponseDto::getPercentage));

            summary.add(BankCashbackSummaryDto.builder()
                    .bankName(bankName)
                    .totalCashbackCategories(cashbacks.size())
                    .bestCashbackPercentage(bestCashback.map(CashbackResponseDto::getPercentage).orElse(BigDecimal.ZERO))
                    .bestCashbackCategory(bestCashback.map(CashbackResponseDto::getCategory).orElse("Нет"))
                    .cashbackByCategory(cashbackByCategory)
                    .activeCashbacks(cashbacks)
                    .build());
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