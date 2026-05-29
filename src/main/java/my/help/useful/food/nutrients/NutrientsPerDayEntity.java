package my.help.useful.food.nutrients;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "nutrients_per_day")
public class NutrientsPerDayEntity {

    @Id
    private LocalDate date;

    private int calories;
    private double protein;
    private double fat;
    private double carbohydrates;
    private double fiber;
}
