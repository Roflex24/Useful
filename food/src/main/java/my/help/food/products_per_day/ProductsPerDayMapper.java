package my.help.food.products_per_day;

import my.help.food.nutrients.dto.ProductsPerDayResponse;
import my.help.food.product.dto.ProductResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductsPerDayMapper {

    @Mapping(target = ".", source = "productResponse")
    @Mapping(target = "quantity", source = "quantity")
    ProductsPerDayResponse toResponse(ProductResponse productResponse, Double quantity);
}