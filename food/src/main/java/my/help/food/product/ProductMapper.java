package my.help.food.product;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    ProductModel toModel(ProductEntity productEntity);
    ProductEntity toEntity(ProductModel productModel);

    List<ProductModel> toModelList(List<ProductEntity> productEntities);

    @Mapping(target = "id", ignore = true)
    void updateEntityFromModel(ProductModel productModel, @MappingTarget ProductEntity productEntity);
}