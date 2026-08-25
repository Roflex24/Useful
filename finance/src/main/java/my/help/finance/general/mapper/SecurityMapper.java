package my.help.finance.general.mapper;

import lombok.RequiredArgsConstructor;
import my.help.finance.general.dto.SecurityRequestDto;
import my.help.finance.general.dto.SecurityResponseDto;
import my.help.finance.general.entity.Account;
import my.help.finance.general.entity.AccountType;
import my.help.finance.general.entity.Security;
import my.help.finance.general.entity.SecurityType;
import my.help.finance.general.repository.AccountRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SecurityMapper {

    private final AccountRepository accountRepository;

    public Security toEntity(SecurityRequestDto requestDto) {
        Account account = accountRepository.findById(requestDto.accountId())
                .orElseThrow(() -> new RuntimeException("Account not found"));

        if (account.getType() != AccountType.INVESTMENT) {
            throw new RuntimeException("Securities can only be added to INVESTMENT accounts. Current type: " + account.getType());
        }

        Security security = Security.builder()
                .account(account)
                .securityType(requestDto.securityType())
                .ticker(requestDto.ticker())
                .quantity(requestDto.quantity())
                .currentPrice(requestDto.currentPrice())
                .build();

        applyBondFieldsIfNeeded(security, requestDto);
        return security;
    }

    public void updateEntity(Security existing, SecurityRequestDto requestDto) {
        existing.setSecurityType(requestDto.securityType());
        existing.setTicker(requestDto.ticker());
        existing.setQuantity(requestDto.quantity());
        existing.setCurrentPrice(requestDto.currentPrice());

        // Сбрасываем bond-поля перед применением, на случай смены типа бумаги
        existing.setFaceValue(null);
        existing.setCouponRate(null);
        existing.setMaturityDate(null);
        applyBondFieldsIfNeeded(existing, requestDto);
    }

    private void applyBondFieldsIfNeeded(Security security, SecurityRequestDto requestDto) {
        if (requestDto.securityType() == SecurityType.BOND) {
            security.setFaceValue(requestDto.faceValue());
            security.setCouponRate(requestDto.couponRate());
            security.setMaturityDate(requestDto.maturityDate());
        }
    }

    public SecurityResponseDto toResponseDto(Security security) {
        return new SecurityResponseDto(
                security.getId(),
                security.getAccount().getId(),
                security.getAccount().getBankName(),
                security.getSecurityType(),
                security.getTicker(),
                security.getQuantity(),
                security.getCurrentPrice(),
                security.getTotalValue(),
                security.getFaceValue(),
                security.getCouponRate(),
                security.getMaturityDate()
        );
    }
}