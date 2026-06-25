package my.help.finance.entity;

import lombok.Getter;

@Getter
public enum AccountType {
    CARD("Карта/счёт"),
    DEPOSIT("Вклад"),
    SAVINGS("Накопительный счёт"),
    INVESTMENT("Инвестиции");

    private final String description;

    AccountType(String description) {
        this.description = description;
    }

}