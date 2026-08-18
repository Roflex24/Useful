package my.help.food.product;

import my.help.food.product.dto.ProductRequest;
import my.help.food.product.dto.ProductResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    ProductResponse toResponse(ProductEntity entity);
    List<ProductResponse> toResponseList(List<ProductEntity> entities);

    @Mapping(target = "id", ignore = true)
    ProductEntity toEntity(ProductRequest request);

    @Mapping(target = "id", ignore = true)
    void updateEntityFromRequest(ProductRequest request, @MappingTarget ProductEntity entity);
}