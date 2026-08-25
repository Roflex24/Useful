package my.help.finance.general.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.help.finance.general.dto.SecurityRequestDto;
import my.help.finance.general.dto.SecurityResponseDto;
import my.help.finance.general.entity.Account;
import my.help.finance.general.entity.AccountType;
import my.help.finance.general.entity.Security;
import my.help.finance.general.mapper.SecurityMapper;
import my.help.finance.general.repository.AccountRepository;
import my.help.finance.general.repository.SecurityRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SecurityService {

    private final SecurityRepository securityRepository;
    private final AccountRepository accountRepository;
    private final SecurityMapper securityMapper;

    public List<SecurityResponseDto> getAllSecurities() {
        log.debug("Fetching all securities");
        return securityRepository.findAll()
                .stream()
                .map(securityMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    public List<SecurityResponseDto> getSecuritiesByAccount(Long accountId) {
        log.debug("Fetching securities for account: {}", accountId);

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found with id: " + accountId));

        if (account.getType() != AccountType.INVESTMENT) {
            log.warn("Attempt to get securities for non-investment account: {} (type: {})", accountId, account.getType());
            return Collections.emptyList();
        }

        return securityRepository.findByAccountId(accountId)
                .stream()
                .map(securityMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    public List<Security> getSecuritiesByAccountEntity(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        if (account.getType() != AccountType.INVESTMENT) {
            return Collections.emptyList();
        }

        return securityRepository.findByAccount(account);
    }

    @Transactional
    public SecurityResponseDto createSecurity(SecurityRequestDto requestDto) {
        log.info("Creating security for account: {}", requestDto.getAccountId());

        Security security = securityMapper.toEntity(requestDto);
        Security savedSecurity = securityRepository.save(security);
        log.info("Security created successfully with id: {}", savedSecurity.getId());

        recalculateAccountAmount(requestDto.getAccountId());

        return securityMapper.toResponseDto(savedSecurity);
    }

    @Transactional
    public SecurityResponseDto updateSecurity(Long id, SecurityRequestDto requestDto) {
        log.info("Updating security with id: {}", id);

        Security existingSecurity = securityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Security not found with id: " + id));

        Account account = existingSecurity.getAccount();
        if (account.getType() != AccountType.INVESTMENT) {
            throw new RuntimeException("Cannot update security for non-investment account");
        }

        securityMapper.updateEntity(existingSecurity, requestDto);
        Security updatedSecurity = securityRepository.save(existingSecurity);
        log.info("Security updated successfully with id: {}", id);

        recalculateAccountAmount(account.getId());

        return securityMapper.toResponseDto(updatedSecurity);
    }

    @Transactional
    public void deleteSecurity(Long id) {
        log.info("Deleting security with id: {}", id);

        Security security = securityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Security not found with id: " + id));

        Long accountId = security.getAccount().getId();

        securityRepository.deleteById(id);
        log.info("Security deleted successfully with id: {}", id);

        recalculateAccountAmount(accountId);
    }

    /**
     * Удалить все бумаги счёта (используется при удалении самого счёта)
     */
    @Transactional
    public void deleteAllSecuritiesForAccount(Long accountId) {
        List<Security> securities = securityRepository.findByAccountId(accountId);
        if (!securities.isEmpty()) {
            securityRepository.deleteAll(securities);
            log.info("Deleted {} securities for account id: {}", securities.size(), accountId);
        }
    }

    /**
     * Пересчитать Account.amount как сумму (quantity * currentPrice) по всем бумагам счёта.
     * Вызывается после любого изменения состава бумаг.
     */
    @Transactional
    public void recalculateAccountAmount(Long accountId) {
        BigDecimal total = securityRepository.getTotalValueByAccountId(accountId);
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found with id: " + accountId));
        account.setAmount(total);
        accountRepository.save(account);
        log.debug("Recalculated amount for INVESTMENT account {}: {}", accountId, total);
    }
}