package my.help.useful.calculate_calories.products_per_day;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ProductsPerDayKeyEntity {

    private LocalDate date;
    private Long productId;
}
