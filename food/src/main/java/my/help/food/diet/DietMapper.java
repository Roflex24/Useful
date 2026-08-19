package my.help.food.diet;

import my.help.food.diet.dto.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DietMapper {

    DietRs toResponse(DietEntity entity);
    List<DietRs> toResponseList(List<DietEntity> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "items", ignore = true)
    DietEntity toEntity(DietRq request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "items", ignore = true)
    void updateEntityFromRequest(DietRq request, @MappingTarget DietEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "diet", source = "diet")
    DietItemEntity toItemEntity(DietItemRq request, DietEntity diet);

    DietItemRs toItemResponse(DietItemEntity entity);
    List<DietItemRs> toItemResponseList(List<DietItemEntity> entities);
}