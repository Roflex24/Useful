package my.help.food.common.enums;

import lombok.Getter;

@Getter
public enum Macronutrients {

    CALORIES("Калории"),
    PROTEIN("Белки"),
    FAT("Жиры"),
    CARBOHYDRATE("Углеводы"),
    FIBER("Клетчатка");

    private final String displayName;

    Macronutrients(String displayName) {
        this.displayName = displayName;
    }

}
