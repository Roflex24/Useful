package my.help.food.diet;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DietMapper {

    DietItemModel toItemModel(DietItemEntity dietItemEntity);
    List<DietItemModel> toItemModelList(List<DietItemEntity> dietItemEntities);

    DietModel toModel(DietEntity dietEntity);
    List<DietModel> toModelList(List<DietEntity> dietEntities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "items", ignore = true)
    DietEntity toEntity(DietModel dietModel);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "items", ignore = true)
    void updateEntityFromModel(DietModel dietModel, @MappingTarget DietEntity dietEntity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "diet", source = "diet")
    DietItemEntity toItemEntity(DietItemModel dietItemModel, DietEntity diet);
}