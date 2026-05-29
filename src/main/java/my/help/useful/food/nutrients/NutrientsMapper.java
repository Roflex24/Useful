package my.help.useful.food.nutrients;

import org.mapstruct.Mapper;
import java.util.List;

@Mapper(componentModel = "spring")
public interface NutrientsMapper {

    NutrientsModel toModel(NutrientsPerDayEntity nutrientsPerDayEntity);
    NutrientsPerDayEntity toEntity(NutrientsModel nutrientsModel);

    List<NutrientsModel> toModelList(List<NutrientsPerDayEntity> nutrientsPerDayEntityList);

}
