package my.help.food.common.enums;

import lombok.Getter;

@Getter
public enum Shop {
    CHISHIK("Чижик"),
    SMART("Смарт"),
    PIATIOROCHKA("Пятерочка"),
    MAGNIT("Магнит"),
    SVETOFOR("Светофор");

    private final String displayName;

    Shop(String displayName) {
        this.displayName = displayName;
    }
}