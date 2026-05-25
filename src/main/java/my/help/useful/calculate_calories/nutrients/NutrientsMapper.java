package my.help.useful.calculate_calories.nutrients;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface NutrientsMapper {

    NutrientsMapper INSTANCE = Mappers.getMapper(NutrientsMapper.class);

    NutrientsModel toModel(NutrientsPerDayEntity nutrientsPerDayEntity);
    NutrientsPerDayEntity toEntity(NutrientsModel nutrientsModel);

    List<NutrientsModel> toModelList(List<NutrientsPerDayEntity> nutrientsPerDayEntityList);

}
