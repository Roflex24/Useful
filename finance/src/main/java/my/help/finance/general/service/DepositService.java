package my.help.finance.general.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.help.finance.common.BusinessValidationException;
import my.help.finance.common.ConflictException;
import my.help.finance.common.ResourceNotFoundException;
import my.help.finance.general.dto.DepositInfoDto;
import my.help.finance.general.entity.Account;
import my.help.finance.general.entity.AccountType;
import my.help.finance.general.entity.Deposit;
import my.help.finance.general.mapper.DepositMapper;
import my.help.finance.general.repository.DepositRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DepositService {

    private final DepositRepository depositRepository;
    private final DepositMapper depositMapper;

    @Transactional
    public void createDeposit(Account account, DepositInfoDto depositInfo) {
        if (account.getType() != AccountType.DEPOSIT && account.getType() != AccountType.SAVINGS) {
            throw new BusinessValidationException("Deposit can only be created for DEPOSIT or SAVINGS accounts");
        }

        if (depositInfo == null) {
            throw new BusinessValidationException("Deposit info is required for " + account.getType() + " account type");
        }

        if (account.getType() == AccountType.DEPOSIT && depositInfo.endDate() == null) {
            throw new BusinessValidationException("End date is required for DEPOSIT account type");
        }

        if (depositRepository.findByAccountId(account.getId()).isPresent()) {
            throw new ConflictException("Deposit already exists for this account");
        }

        Deposit deposit = depositMapper.toEntity(depositInfo, account);
        depositRepository.save(deposit);
        log.info("Deposit created for account id: {}", account.getId());

    }

    @Transactional
    public void updateDeposit(Long accountId, DepositInfoDto depositInfo) {
        if (depositInfo == null) {
            deleteDeposit(accountId);
            return;
        }

        Deposit existingDeposit = depositRepository.findByAccountId(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Deposit not found for account id: " + accountId));

        depositMapper.updateEntity(existingDeposit, depositInfo);
        depositRepository.save(existingDeposit);
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