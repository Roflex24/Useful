package my.help.useful.kanban.planning.mapper;

import my.help.useful.kanban.planning.dto.PlanRequestDto;
import my.help.useful.kanban.planning.dto.PlanResponseDto;
import my.help.useful.kanban.planning.entity.StrategicPlan;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PlanningMapper {

    void updateEntity(PlanRequestDto dto, @MappingTarget StrategicPlan plan);
    StrategicPlan rqToEntity(PlanRequestDto dto);
    PlanResponseDto toResponseDto(StrategicPlan plan);
    List<PlanResponseDto> toResponseDtoList(List<StrategicPlan> plans);
}
