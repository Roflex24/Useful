package my.help.finance.general.mapper;

import my.help.finance.general.dto.AccountRequestDto;
import my.help.finance.general.dto.AccountResponseDto;
import my.help.finance.general.entity.Account;
import org.springframework.stereotype.Component;

@Component
public class AccountMapper {

    public Account toEntity(AccountRequestDto requestDto) {
        return Account.builder()
                .bankName(requestDto.getBankName())
                .amount(requestDto.getAmount())
                .type(requestDto.getType())
                .comment(requestDto.getComment())
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
        existingAccount.setBankName(requestDto.getBankName());
        existingAccount.setAmount(requestDto.getAmount());
        existingAccount.setType(requestDto.getType());
        existingAccount.setComment(requestDto.getComment());
    }
}