package my.help.food.diet;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DietMapper {

    @Mapping(target = "productId", source = "productId")
    @Mapping(target = "quantity", source = "quantity")
    DietItemModel toItemModel(DietItemEntity dietItemEntity);

    List<DietItemModel> toItemModelList(List<DietItemEntity> dietItemEntities);

    @Mapping(target = "items", source = "items")
    DietModel toModel(DietEntity dietEntity);

    List<DietModel> toModelList(List<DietEntity> dietEntities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "items", ignore = true)
    DietEntity toEntity(DietModel dietModel);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "items", ignore = true)
    void updateEntityFromModel(DietModel dietModel, @MappingTarget DietEntity dietEntity);
}
