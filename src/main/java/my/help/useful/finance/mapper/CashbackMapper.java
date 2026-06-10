package my.help.useful.finance.mapper;

import lombok.RequiredArgsConstructor;
import my.help.useful.finance.dto.CashbackRequestDto;
import my.help.useful.finance.dto.CashbackResponseDto;
import my.help.useful.finance.entity.Account;
import my.help.useful.finance.entity.AccountType;
import my.help.useful.finance.entity.Cashback;
import my.help.useful.finance.repository.AccountRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CashbackMapper {

    private final AccountRepository accountRepository;

    public Cashback toEntity(CashbackRequestDto requestDto) {
        Account account = accountRepository.findById(requestDto.getAccountId())
                .orElseThrow(() -> new RuntimeException("Account not found"));

        if (account.getType() != AccountType.CARD) {
            throw new RuntimeException("Cashback can only be added to CARD accounts. Current type: " + account.getType());
        }

        return Cashback.builder()
                .account(account)
                .category(requestDto.getCategory())
                .percentage(requestDto.getPercentage())
                .build();
    }

    public CashbackResponseDto toResponseDto(Cashback cashback) {
        return CashbackResponseDto.builder()
                .id(cashback.getId())
                .accountId(cashback.getAccount().getId())
                .bankName(cashback.getAccount().getBankName())
                .category(cashback.getCategory())
                .percentage(cashback.getPercentage())
                .build();
    }
}