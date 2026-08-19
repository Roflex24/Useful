package my.help.food.nutrients;

import my.help.food.nutrients.dto.NutrientsRs;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface NutrientsMapper {

    List<NutrientsRs> toResponseList(List<NutrientsPerDayEntity> entities);
}