package my.help.useful.food.products_per_day;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.time.LocalDate;

@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class ProductsPerDayKeyEntity {

    private LocalDate date;
    private Long productId;
}
