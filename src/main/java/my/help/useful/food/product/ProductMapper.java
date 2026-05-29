package my.help.useful.food.product;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    ProductModel toModel(ProductEntity productEntity);
    ProductEntity toEntity(ProductModel productModel);

    List<ProductModel> toModelList(List<ProductEntity> productEntities);

    void updateEntityFromModel(ProductModel productModel, @MappingTarget ProductEntity productEntity);
}
