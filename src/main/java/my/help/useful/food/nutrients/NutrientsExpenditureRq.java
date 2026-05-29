package my.help.useful.food.nutrients;

import lombok.Data;

@Data
public class NutrientsExpenditureRq {
    private String target;
    private String gender;        // "male" или "female"
    private double weightKg;      // вес в кг
    private double heightCm;      // рост в см
    private int ageYears;         // возраст в годах
    private int steps;            // количество шагов за день
}