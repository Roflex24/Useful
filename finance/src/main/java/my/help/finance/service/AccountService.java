package my.help.finance.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.help.finance.entity.Cashback;
import my.help.finance.dto.AccountRequestDto;
import my.help.finance.dto.AccountResponseDto;
import my.help.finance.dto.HistoricalDataResponseDto;
import my.help.finance.entity.Account;
import my.help.finance.entity.AccountType;
import my.help.finance.mapper.AccountMapper;
import my.help.finance.repository.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.util.Collections;
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
    private final FinanceSnapshotService snapshotService;

    public List<AccountResponseDto> getAllAccounts() {
        log.debug("Fetching all accounts");
        return accountRepository.findAll()
                .stream()
                .map(account -> {
                    AccountResponseDto dto = accountMapper.toResponseDto(account);
                    // Показываем кешбеки только для CARD счетов
                    if (account.getType() == AccountType.CARD) {
                        dto.setCashbacks(cashbackService.getCashbacksByAccount(account.getId()));
                    } else {
                        dto.setCashbacks(List.of()); // Пустой список для не-card счетов
                    }
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public AccountResponseDto getAccountById(Long id) {
        log.debug("Fetching account with id: {}", id);
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not found with id: " + id));
        AccountResponseDto dto = accountMapper.toResponseDto(account);
        // Показываем кешбеки только для CARD счетов
        if (account.getType() == AccountType.CARD) {
            dto.setCashbacks(cashbackService.getCashbacksByAccount(id));
        } else {
            dto.setCashbacks(List.of());
        }
        return dto;
    }

    @Transactional
    public AccountResponseDto createAccount(AccountRequestDto requestDto) {
        log.info("Creating new account: {}", requestDto.getBankName());

        Account account = accountMapper.toEntity(requestDto);
        Account savedAccount = accountRepository.save(account);
        log.info("Account created successfully with id: {}", savedAccount.getId());

        AccountResponseDto dto = accountMapper.toResponseDto(savedAccount);
        dto.setCashbacks(List.of());
        return dto;
    }

    @Transactional
    public AccountResponseDto updateAccount(Long id, AccountRequestDto requestDto) {
        log.info("Updating account with id: {}", id);

        Account existingAccount = accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not found with id: " + id));

        // Если тип меняется с CARD на другой, нужно удалить связанные кешбеки
        AccountType oldType = existingAccount.getType();
        AccountType newType = requestDto.getType();

        accountMapper.updateEntity(existingAccount, requestDto);

        // Если тип меняется с CARD на другой, удаляем все кешбеки
        if (oldType == AccountType.CARD && newType != AccountType.CARD) {
            List<Cashback> cashbacks = cashbackService.getCashbacksByAccountEntity(id);
            for (var cb : cashbacks) {
                cashbackService.deleteCashback(cb.getId());
            }
            log.info("Deleted all cashbacks for account {} because type changed from CARD to {}", id, newType);
        }

        Account updatedAccount = accountRepository.save(existingAccount);
        log.info("Account updated successfully with id: {}", id);

        AccountResponseDto dto = accountMapper.toResponseDto(updatedAccount);
        if (updatedAccount.getType() == AccountType.CARD) {
            dto.setCashbacks(cashbackService.getCashbacksByAccount(id));
        } else {
            dto.setCashbacks(List.of());
        }
        return dto;
    }

    @Transactional
    public void deleteAccount(Long id) {
        log.info("Deleting account with id: {}", id);

        if (!accountRepository.existsById(id)) {
            throw new RuntimeException("Account not found with id: " + id);
        }

        accountRepository.deleteById(id);
        log.info("Account deleted successfully with id: {}", id);
    }

    /**
     * Получить исторические счета за указанный месяц
     */
    @Transactional(readOnly = true)
    public List<AccountResponseDto> getHistoricalAccounts(YearMonth yearMonth) {
        HistoricalDataResponseDto historicalData = snapshotService.getHistoricalData(yearMonth);
        return historicalData != null ? historicalData.getAccounts() : Collections.emptyList();
    }
}