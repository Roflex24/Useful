package my.help.finance.entity;

import lombok.Getter;

@Getter
public enum SecurityType {
    STOCK("Акция"),
    BOND("Облигация"),
    ETF("ETF/Фонд"),
    CURRENCY_METAL("Валюта/Металл");

    private final String description;

    SecurityType(String description) {
        this.description = description;
    }
}