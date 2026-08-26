package my.help.finance.general.mapper;

import my.help.finance.general.dto.DepositInfoDto;
import my.help.finance.general.entity.Account;
import my.help.finance.general.entity.Deposit;
import org.springframework.stereotype.Component;

@Component
public class DepositMapper {

    public Deposit toEntity(DepositInfoDto dto, Account account) {
        if (dto == null) {
            return null;
        }

        return Deposit.builder()
                .account(account)
                .endDate(dto.endDate())
                .interestPaymentDate(dto.interestPaymentDate())
                .interestRate(dto.interestRate())
                .build();
    }

    public DepositInfoDto toDto(Deposit deposit) {
        if (deposit == null) {
            return null;
        }

        return new DepositInfoDto(
                deposit.getId(),
                deposit.getEndDate(),
                deposit.getInterestPaymentDate(),
                deposit.getInterestRate()
        );
    }

    public void updateEntity(Deposit existingDeposit, DepositInfoDto dto) {
        if (dto == null) {
            return;
        }
        existingDeposit.setEndDate(dto.endDate());
        existingDeposit.setInterestPaymentDate(dto.interestPaymentDate());
        existingDeposit.setInterestRate(dto.interestRate());
    }
}