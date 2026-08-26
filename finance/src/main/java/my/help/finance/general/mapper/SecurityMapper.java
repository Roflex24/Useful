package my.help.finance.general.mapper;

import my.help.finance.general.dto.SecurityRq;
import my.help.finance.general.dto.SecurityRs;
import my.help.finance.general.entity.Account;
import my.help.finance.general.entity.Security;
import my.help.finance.general.entity.SecurityType;
import org.springframework.stereotype.Component;

@Component
public class SecurityMapper {

    public Security toEntity(SecurityRq requestDto, Account account) {
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

    public void updateEntity(Security existing, SecurityRq requestDto) {
        existing.setSecurityType(requestDto.securityType());
        existing.setTicker(requestDto.ticker());
        existing.setQuantity(requestDto.quantity());
        existing.setCurrentPrice(requestDto.currentPrice());

        existing.setFaceValue(null);
        existing.setCouponRate(null);
        existing.setMaturityDate(null);
        applyBondFieldsIfNeeded(existing, requestDto);
    }

    private void applyBondFieldsIfNeeded(Security security, SecurityRq requestDto) {
        if (requestDto.securityType() == SecurityType.BOND) {
            security.setFaceValue(requestDto.faceValue());
            security.setCouponRate(requestDto.couponRate());
            security.setMaturityDate(requestDto.maturityDate());
        }
    }

    public SecurityRs toResponseDto(Security security) {
        return new SecurityRs(
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