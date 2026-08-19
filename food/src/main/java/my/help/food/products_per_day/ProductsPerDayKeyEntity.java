package my.help.food.products_per_day;

import jakarta.persistence.Embeddable;
import lombok.*;

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
