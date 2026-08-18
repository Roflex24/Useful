package my.help.food.product;

import my.help.food.product.dto.ProductRq;
import my.help.food.product.dto.ProductRs;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    ProductRs toResponse(ProductEntity entity);

    @Mapping(target = "id", ignore = true)
    ProductEntity toEntity(ProductRq request);

    @Mapping(target = "id", ignore = true)
    void updateEntityFromRequest(ProductRq request, @MappingTarget ProductEntity entity);
}