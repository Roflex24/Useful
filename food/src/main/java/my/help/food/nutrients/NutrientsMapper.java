package my.help.food.nutrients;

import my.help.food.nutrients.dto.NutrientsRs;
import my.help.food.nutrients.dto.ProductsPerDayRs;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface NutrientsMapper {

    List<NutrientsRs> toResponseList(List<NutrientsPerDayEntity> entities);

    @Mapping(target = "id", source = "productId")
    @Mapping(target = "name", source = "productName")
    @Mapping(target = "calories", source = "productCalories")
    @Mapping(target = "protein", source = "productProtein")
    @Mapping(target = "fat", source = "productFat")
    @Mapping(target = "carbohydrate", source = "productCarbohydrate")
    @Mapping(target = "fiber", source = "productFiber")
    @Mapping(target = "unit", source = "unit")
    @Mapping(target = "photoUrl", source = "photoUrl")
    @Mapping(target = "shop", source = "shop")
    @Mapping(target = "quantity", source = "quantity")
    ProductsPerDayRs toProductsPerDayRs(NutrientsWithProductsRowProjection row);
}