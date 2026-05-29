package my.help.useful.food.products_per_day;

import my.help.useful.food.product.ProductModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductsPerDayMapper {

    @Mapping(target = ".", source = "productModel")
    @Mapping(target = "quantity", source = "quantity")
    ProductsPerDayModel toModel(ProductModel productModel, Double quantity);
}