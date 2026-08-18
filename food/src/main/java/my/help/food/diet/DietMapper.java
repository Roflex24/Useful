package my.help.food.diet;

import my.help.food.diet.dto.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DietMapper {

    DietResponse toResponse(DietEntity entity);
    List<DietResponse> toResponseList(List<DietEntity> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "items", ignore = true)
    DietEntity toEntity(DietRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "items", ignore = true)
    void updateEntityFromRequest(DietRequest request, @MappingTarget DietEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "diet", source = "diet")
    DietItemEntity toItemEntity(DietItemRequest request, DietEntity diet);

    DietItemResponse toItemResponse(DietItemEntity entity);
    List<DietItemResponse> toItemResponseList(List<DietItemEntity> entities);
}