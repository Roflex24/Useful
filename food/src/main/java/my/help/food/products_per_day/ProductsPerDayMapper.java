package my.help.food.products_per_day;

import my.help.food.nutrients.dto.ProductsPerDayRs;
import my.help.food.product.dto.ProductRs;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductsPerDayMapper {

    @Mapping(target = "id", source = "product.id")
    @Mapping(target = "name", source = "product.name")
    @Mapping(target = "calories", source = "product.calories")
    @Mapping(target = "protein", source = "product.protein")
    @Mapping(target = "fat", source = "product.fat")
    @Mapping(target = "carbohydrate", source = "product.carbohydrate")
    @Mapping(target = "fiber", source = "product.fiber")
    @Mapping(target = "unit", source = "product.unit")
    @Mapping(target = "photoUrl", source = "product.photoUrl")
    @Mapping(target = "shop", source = "product.shop")
    @Mapping(target = "quantity", source = "quantity")
    ProductsPerDayRs toResponse(ProductRs product, Double quantity);
}