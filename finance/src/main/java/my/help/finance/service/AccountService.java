package my.help.finance.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.help.finance.dto.AccountRequestDto;
import my.help.finance.dto.AccountResponseDto;
import my.help.finance.entity.Account;
import my.help.finance.entity.AccountType;
import my.help.finance.entity.Cashback;
import my.help.finance.mapper.AccountMapper;
import my.help.finance.repository.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccountService {

    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;
    private final CashbackService cashbackService;
    private final DepositService depositService;
    private final SecurityService securityService; // Добавлено
    private final FinanceSnapshotService snapshotService;

    private boolean hasDepositInfo(AccountType type) {
        return type == AccountType.DEPOSIT || type == AccountType.SAVINGS;
    }

    public List<AccountResponseDto> getAllAccounts() {
        log.debug("Fetching all accounts");
        return accountRepository.findAll()
                .stream()
                .map(this::toResponseDtoWithDetails)
                .sorted(Comparator.comparing(AccountResponseDto::getType)
                        .thenComparing(AccountResponseDto::getAmount, Comparator.reverseOrder()))
                .collect(Collectors.toList());
    }

    public AccountResponseDto getAccountById(Long id) {
        log.debug("Fetching account with id: {}", id);
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not found with id: " + id));
        return toResponseDtoWithDetails(account);
    }

    private AccountResponseDto toResponseDtoWithDetails(Account account) {
        AccountResponseDto dto = accountMapper.toResponseDto(account);

        if (account.getType() == AccountType.CARD) {
            dto.setCashbacks(cashbackService.getCashbacksByAccount(account.getId()));
        } else {
            dto.setCashbacks(Collections.emptyList());
        }

        if (hasDepositInfo(account.getType())) {
            dto.setDepositInfo(depositService.getDepositByAccountId(account.getId()));
        } else {
            dto.setDepositInfo(null);
        }

        // Добавляем бумаги для INVESTMENT
        if (account.getType() == AccountType.INVESTMENT) {
            dto.setSecurities(securityService.getSecuritiesByAccount(account.getId()));
        } else {
            dto.setSecurities(Collections.emptyList());
        }

        return dto;
    }

    @Transactional
    public AccountResponseDto createAccount(AccountRequestDto requestDto) {
        log.info("Creating new account: {}", requestDto.getBankName());

        if (hasDepositInfo(requestDto.getType()) && requestDto.getDepositInfoDto() == null) {
            throw new RuntimeException("Deposit info is required for " + requestDto.getType() + " account type");
        }

        Account account = accountMapper.toEntity(requestDto);

        // Для INVESTMENT счёт всегда создаётся с amount = 0 — бумаги ещё не добавлены
        if (account.getType() == AccountType.INVESTMENT) {
            account.setAmount(BigDecimal.ZERO);
        }

        Account savedAccount = accountRepository.save(account);
        log.info("Account created successfully with id: {}", savedAccount.getId());

        if (hasDepositInfo(savedAccount.getType()) && requestDto.getDepositInfoDto() != null) {
            depositService.createDeposit(savedAccount, requestDto.getDepositInfoDto());
        }

        return toResponseDtoWithDetails(savedAccount);
    }

    @Transactional
    public AccountResponseDto updateAccount(Long id, AccountRequestDto requestDto) {
        log.info("Updating account with id: {}", id);

        Account existingAccount = accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not found with id: " + id));

        AccountType newType = requestDto.getType();

        accountMapper.updateEntity(existingAccount, requestDto);

        // Для INVESTMENT amount не редактируется вручную — восстанавливаем
        // авто-рассчитанное значение (на случай если форма прислала своё)
        if (newType == AccountType.INVESTMENT) {
            BigDecimal recalculated = securityService.getSecuritiesByAccount(id).stream()
                    .map(s -> s.getTotalValue())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            existingAccount.setAmount(recalculated);
        }

        if (hasDepositInfo(newType)) {
            depositService.updateDeposit(id, requestDto.getDepositInfoDto());
        }

        Account updatedAccount = accountRepository.save(existingAccount);
        log.info("Account updated successfully with id: {}", id);

        return toResponseDtoWithDetails(updatedAccount);
    }


    @Transactional
    public void deleteAccount(Long id) {
        log.info("Deleting account with id: {}", id);

        if (!accountRepository.existsById(id)) {
            throw new RuntimeException("Account not found with id: " + id);
        }

        Account account = accountRepository.findById(id).orElseThrow();
        if (account.getType() == AccountType.CARD) {
            List<Cashback> cashbacks = cashbackService.getCashbacksByAccountEntity(id);
            for (var cb : cashbacks) {
                cashbackService.deleteCashback(cb.getId());
            }
        }
        if (hasDepositInfo(account.getType())) {
            depositService.deleteDeposit(id);
        }
        if (account.getType() == AccountType.INVESTMENT) {
            securityService.deleteAllSecuritiesForAccount(id);
        }

        accountRepository.deleteById(id);
        log.info("Account deleted successfully with id: {}", id);
    }

    public List<AccountResponseDto> getHistoricalAccounts(YearMonth yearMonth) {
        var historicalData = snapshotService.getHistoricalData(yearMonth);
        return historicalData != null ? historicalData.getAccounts() : Collections.emptyList();
    }
}