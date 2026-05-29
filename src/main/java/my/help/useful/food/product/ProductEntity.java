package my.help.useful.food.product;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "products")
public class ProductEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private double calories;
    private double protein;
    private double fat;
    private double carbohydrate;
    private double fiber;
    private String unit;
    private String photoUrl;
    private Shop shop;
}
