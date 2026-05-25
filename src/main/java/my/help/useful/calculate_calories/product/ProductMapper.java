package my.help.useful.calculate_calories.product;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    ProductMapper INSTANCE = Mappers.getMapper(ProductMapper.class);

    ProductModel toModel(ProductEntity productEntity);
    ProductEntity toEntity(ProductModel productModel);

    List<ProductModel> toModelList(List<ProductEntity> productEntities);
    List<ProductEntity> toEntityList(List<ProductModel> productModels);

    void updateEntityFromModel(ProductModel productModel, @MappingTarget ProductEntity productEntity);
}
