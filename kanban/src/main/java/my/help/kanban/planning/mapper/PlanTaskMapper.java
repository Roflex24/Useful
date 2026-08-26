package my.help.kanban.planning.mapper;

import my.help.kanban.planning.dto.PlanTaskRs;
import my.help.kanban.planning.entity.PlanTask;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PlanTaskMapper {

    @Mapping(target = "planId", source = "plan.id")
    PlanTaskRs toResponseDto(PlanTask task);
}
