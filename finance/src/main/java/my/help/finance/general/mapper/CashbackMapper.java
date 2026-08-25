package my.help.finance.general.mapper;

import lombok.RequiredArgsConstructor;
import my.help.finance.general.dto.CashbackRq;
import my.help.finance.general.dto.CashbackRs;
import my.help.finance.general.entity.Account;
import my.help.finance.general.entity.AccountType;
import my.help.finance.general.entity.Cashback;
import my.help.finance.general.repository.AccountRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CashbackMapper {

    private final AccountRepository accountRepository;

    public Cashback toEntity(CashbackRq requestDto) {
        Account account = accountRepository.findById(requestDto.accountId())
                .orElseThrow(() -> new RuntimeException("Account not found"));

        if (account.getType() != AccountType.CARD) {
            throw new RuntimeException("Cashback can only be added to CARD accounts. Current type: " + account.getType());
        }

        return Cashback.builder()
                .account(account)
                .category(requestDto.category())
                .percentage(requestDto.percentage())
                .build();
    }

    public CashbackRs toResponseDto(Cashback cashback) {
        return new CashbackRs(
                cashback.getId(),
                cashback.getAccount().getId(),
                cashback.getAccount().getBankName(),
                cashback.getCategory(),
                cashback.getPercentage()
        );
    }
}