package my.help.finance.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.help.finance.dto.DepositInfoDto;
import my.help.finance.entity.Account;
import my.help.finance.entity.AccountType;
import my.help.finance.entity.Deposit;
import my.help.finance.mapper.DepositMapper;
import my.help.finance.repository.DepositRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DepositService {

    private final DepositRepository depositRepository;
    private final DepositMapper depositMapper;

    @Transactional
    public Deposit createDeposit(Account account, DepositInfoDto depositInfo) {
        if (account.getType() != AccountType.DEPOSIT) {
            throw new RuntimeException("Deposit can only be created for DEPOSIT accounts");
        }

        if (depositInfo == null) {
            throw new RuntimeException("Deposit info is required for DEPOSIT account type");
        }

        if (depositRepository.findByAccountId(account.getId()).isPresent()) {
            throw new RuntimeException("Deposit already exists for this account");
        }

        Deposit deposit = depositMapper.toEntity(depositInfo, account);
        Deposit savedDeposit = depositRepository.save(deposit);
        log.info("Deposit created for account id: {}", account.getId());

        return savedDeposit;
    }

    @Transactional
    public Deposit updateDeposit(Long accountId, DepositInfoDto depositInfo) {
        if (depositInfo == null) {
            deleteDeposit(accountId);
            return null;
        }

        Deposit existingDeposit = depositRepository.findByAccountId(accountId)
                .orElseThrow(() -> new RuntimeException("Deposit not found for account id: " + accountId));

        depositMapper.updateEntity(existingDeposit, depositInfo);
        return depositRepository.save(existingDeposit);
    }

    @Transactional
    public void deleteDeposit(Long accountId) {
        depositRepository.findByAccountId(accountId)
                .ifPresent(deposit -> {
                    depositRepository.delete(deposit);
                    log.info("Deposit deleted for account id: {}", accountId);
                });
    }

    public DepositInfoDto getDepositByAccountId(Long accountId) {
        return depositRepository.findByAccountId(accountId)
                .map(depositMapper::toDto)
                .orElse(null);
    }
}