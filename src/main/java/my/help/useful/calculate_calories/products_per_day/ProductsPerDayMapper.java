package my.help.useful.calculate_calories.products_per_day;

import my.help.useful.calculate_calories.product.ProductModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductsPerDayMapper {

    @Mapping(target = ".", source = "productModel")
    @Mapping(target = "quantity", source = "quantity")
    ProductsPerDayModel toModel(ProductModel productModel, Double quantity);
}