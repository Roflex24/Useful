package my.help.kanban.project;

import my.help.kanban.project.dto.ProjectRs;
import my.help.kanban.project.dto.ProjectRq;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProjectMapper {

    @Mapping(target = "mainProjectMetric", ignore = true)
    @Mapping(target = "mainMetricCompletePercent", ignore = true)
    ProjectRs toModel(Project project);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createDate", ignore = true)
    @Mapping(target = "archived", ignore = true)
    @Mapping(target = "archiveDate", ignore = true)
    Project projectRqToEntity(ProjectRq projectRq);

}