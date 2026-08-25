package my.help.finance.general.mapper;

import my.help.finance.general.dto.AccountRq;
import my.help.finance.general.dto.AccountRs;
import my.help.finance.general.entity.Account;
import org.springframework.stereotype.Component;

@Component
public class AccountMapper {

    public Account toEntity(AccountRq requestDto) {
        return Account.builder()
                .bankName(requestDto.bankName())
                .amount(requestDto.amount())
                .type(requestDto.type())
                .comment(requestDto.comment())
                .build();
    }

    public AccountRs toResponseDto(Account account) {
        return AccountRs.builder()
                .id(account.getId())
                .bankName(account.getBankName())
                .amount(account.getAmount())
                .type(account.getType())
                .comment(account.getComment())
                .build();
    }

    public void updateEntity(Account existingAccount, AccountRq requestDto) {
        existingAccount.setBankName(requestDto.bankName());
        existingAccount.setAmount(requestDto.amount());
        existingAccount.setType(requestDto.type());
        existingAccount.setComment(requestDto.comment());
    }
}