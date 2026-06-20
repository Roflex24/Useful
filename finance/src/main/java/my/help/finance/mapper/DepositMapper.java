package my.help.finance.mapper;

import my.help.finance.dto.DepositInfoDto;
import my.help.finance.entity.Account;
import my.help.finance.entity.Deposit;
import org.springframework.stereotype.Component;

@Component
public class DepositMapper {

    public Deposit toEntity(DepositInfoDto dto, Account account) {
        if (dto == null) {
            return null;
        }

        return Deposit.builder()
                .account(account)
                .endDate(dto.getEndDate())
                .interestPaymentDate(dto.getInterestPaymentDate())
                .interestRate(dto.getInterestRate())
                .build();
    }

    public DepositInfoDto toDto(Deposit deposit) {
        if (deposit == null) {
            return null;
        }

        return DepositInfoDto.builder()
                .id(deposit.getId())
                .endDate(deposit.getEndDate())
                .interestPaymentDate(deposit.getInterestPaymentDate())
                .interestRate(deposit.getInterestRate())
                .build();
    }

    public void updateEntity(Deposit existingDeposit, DepositInfoDto dto) {
        if (dto == null) {
            return;
        }
        existingDeposit.setEndDate(dto.getEndDate());
        existingDeposit.setInterestPaymentDate(dto.getInterestPaymentDate());
        existingDeposit.setInterestRate(dto.getInterestRate());
    }
}