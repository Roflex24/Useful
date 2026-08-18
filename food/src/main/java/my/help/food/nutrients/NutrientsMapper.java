package my.help.food.nutrients;

import my.help.food.nutrients.dto.NutrientsResponse;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface NutrientsMapper {

    NutrientsResponse toResponse(NutrientsPerDayEntity entity);

    List<NutrientsResponse> toResponseList(List<NutrientsPerDayEntity> entities);
}