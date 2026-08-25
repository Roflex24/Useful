package my.help.finance.general.mapper;

import my.help.finance.general.dto.AccountRequestDto;
import my.help.finance.general.dto.AccountResponseDto;
import my.help.finance.general.entity.Account;
import org.springframework.stereotype.Component;

@Component
public class AccountMapper {

    public Account toEntity(AccountRequestDto requestDto) {
        return Account.builder()
                .bankName(requestDto.bankName())
                .amount(requestDto.amount())
                .type(requestDto.type())
                .comment(requestDto.comment())
                .build();
    }

    public AccountResponseDto toResponseDto(Account account) {
        return AccountResponseDto.builder()
                .id(account.getId())
                .bankName(account.getBankName())
                .amount(account.getAmount())
                .type(account.getType())
                .comment(account.getComment())
                .build();
    }

    public void updateEntity(Account existingAccount, AccountRequestDto requestDto) {
        existingAccount.setBankName(requestDto.bankName());
        existingAccount.setAmount(requestDto.amount());
        existingAccount.setType(requestDto.type());
        existingAccount.setComment(requestDto.comment());
    }
}