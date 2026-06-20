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
    private final DepositService depositService; // Добавить
    private final FinanceSnapshotService snapshotService;

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

        // Добавляем кешбеки для CARD
        if (account.getType() == AccountType.CARD) {
            dto.setCashbacks(cashbackService.getCashbacksByAccount(account.getId()));
        } else {
            dto.setCashbacks(Collections.emptyList());
        }

        // Добавляем информацию о депозите для DEPOSIT
        if (account.getType() == AccountType.DEPOSIT) {
            dto.setDepositInfo(depositService.getDepositByAccountId(account.getId()));
        } else {
            dto.setDepositInfo(null);
        }

        return dto;
    }

    @Transactional
    public AccountResponseDto createAccount(AccountRequestDto requestDto) {
        log.info("Creating new account: {}", requestDto.getBankName());

        // Проверка: для DEPOSIT обязательно должен быть depositInfo
        if (requestDto.getType() == AccountType.DEPOSIT && requestDto.getDepositInfoDto() == null) {
            throw new RuntimeException("Deposit info is required for DEPOSIT account type");
        }

        Account account = accountMapper.toEntity(requestDto);
        Account savedAccount = accountRepository.save(account);
        log.info("Account created successfully with id: {}", savedAccount.getId());

        // Создаем депозит, если тип DEPOSIT
        if (savedAccount.getType() == AccountType.DEPOSIT && requestDto.getDepositInfoDto() != null) {
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

        // Обновляем основные поля
        accountMapper.updateEntity(existingAccount, requestDto);

        // Обновляем депозит, если тип DEPOSIT
        if (newType == AccountType.DEPOSIT) {
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

        // Удаляем связанные данные
        Account account = accountRepository.findById(id).orElseThrow();
        if (account.getType() == AccountType.CARD) {
            List<Cashback> cashbacks = cashbackService.getCashbacksByAccountEntity(id);
            for (var cb : cashbacks) {
                cashbackService.deleteCashback(cb.getId());
            }
        }
        if (account.getType() == AccountType.DEPOSIT) {
            depositService.deleteDeposit(id);
        }

        accountRepository.deleteById(id);
        log.info("Account deleted successfully with id: {}", id);
    }

    public List<AccountResponseDto> getHistoricalAccounts(YearMonth yearMonth) {
        var historicalData = snapshotService.getHistoricalData(yearMonth);
        return historicalData != null ? historicalData.getAccounts() : Collections.emptyList();
    }
}