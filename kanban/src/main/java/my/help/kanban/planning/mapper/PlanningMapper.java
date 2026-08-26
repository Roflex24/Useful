package my.help.kanban.planning.mapper;

import my.help.kanban.planning.dto.PlanRq;
import my.help.kanban.planning.dto.PlanRs;
import my.help.kanban.planning.entity.StrategicPlan;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PlanningMapper {

    @Mapping(target = "id", ignore = true)
    void updateEntity(PlanRq dto, @MappingTarget StrategicPlan plan);

    @Mapping(target = "id", ignore = true)
    StrategicPlan rqToEntity(PlanRq dto);

    PlanRs toResponseDto(StrategicPlan plan);
    List<PlanRs> toResponseDtoList(List<StrategicPlan> plans);
}