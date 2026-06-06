package my.help.useful.finance.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.help.useful.finance.dto.AccountRequestDto;
import my.help.useful.finance.dto.AccountResponseDto;
import my.help.useful.finance.entity.Account;
import my.help.useful.finance.mapper.AccountMapper;
import my.help.useful.finance.repository.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccountService {

    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;

    public List<AccountResponseDto> getAllAccounts() {
        log.debug("Fetching all accounts");
        return accountRepository.findAll()
                .stream()
                .map(accountMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    public AccountResponseDto getAccountById(Long id) {
        log.debug("Fetching account with id: {}", id);
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not found with id: " + id));
        return accountMapper.toResponseDto(account);
    }

    @Transactional
    public AccountResponseDto createAccount(AccountRequestDto requestDto) {
        log.info("Creating new account: {}", requestDto.getBankName());

        Account account = accountMapper.toEntity(requestDto);
        Account savedAccount = accountRepository.save(account);
        log.info("Account created successfully with id: {}", savedAccount.getId());

        return accountMapper.toResponseDto(savedAccount);
    }

    @Transactional
    public AccountResponseDto updateAccount(Long id, AccountRequestDto requestDto) {
        log.info("Updating account with id: {}", id);

        Account existingAccount = accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not found with id: " + id));

        accountMapper.updateEntity(existingAccount, requestDto);
        Account updatedAccount = accountRepository.save(existingAccount);
        log.info("Account updated successfully with id: {}", id);

        return accountMapper.toResponseDto(updatedAccount);
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
}