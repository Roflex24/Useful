package my.help.kanban.planning.mapper;

import my.help.kanban.planning.dto.PlanRequestDto;
import my.help.kanban.planning.dto.PlanResponseDto;
import my.help.kanban.planning.entity.StrategicPlan;
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