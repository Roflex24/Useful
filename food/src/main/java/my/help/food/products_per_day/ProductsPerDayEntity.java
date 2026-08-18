package my.help.food.products_per_day;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "products_per_day")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductsPerDayEntity {

    @EmbeddedId
    private ProductsPerDayKeyEntity id;

    @Column(nullable = false)
    private Double quantity;
}

//todo добавить защиту чтобы нельзя было удалять продукты если они где-то используются