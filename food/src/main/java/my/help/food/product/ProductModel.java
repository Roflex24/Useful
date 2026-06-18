package my.help.food.product;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductModel {

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
