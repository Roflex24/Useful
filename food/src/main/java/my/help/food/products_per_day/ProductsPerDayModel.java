package my.help.food.products_per_day;

import lombok.*;
import my.help.food.product.Shop;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class ProductsPerDayModel {
    private Long id;
    private String name;
    private Double calories;
    private Double protein;
    private Double fat;
    private Double carbohydrate;
    private Double fiber;
    private String unit;
    private String photoUrl;
    private Shop shop;
    private Double quantity;
}