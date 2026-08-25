package my.help.finance.general.mapper;

import lombok.RequiredArgsConstructor;
import my.help.finance.general.dto.CashbackRq;
import my.help.finance.general.dto.CashbackRs;
import my.help.finance.general.entity.Account;
import my.help.finance.general.entity.Cashback;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CashbackMapper {

    public Cashback toEntity(CashbackRq requestDto, Account account) {
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