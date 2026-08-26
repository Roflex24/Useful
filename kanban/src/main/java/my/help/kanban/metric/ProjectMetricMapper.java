package my.help.kanban.metric;

import my.help.kanban.metric.dto.ProjectMetricRs;
import my.help.kanban.metric.dto.ProjectMetricRq;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProjectMetricMapper {

    @Mapping(target = "project", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "main", source = "isMain")
    @Mapping(target = "complete", source = "isComplete")
    ProjectMetricEntity projectMetricRqToEntity(ProjectMetricRq projectMetricRq);

    @Mapping(target = "project", ignore = true)
    ProjectMetricEntity toEntity(ProjectMetricRs projectMetricRs);

    @Mapping(target = "projectId", ignore = true)
    ProjectMetricRs toModel(ProjectMetricEntity projectMetricEntity);

    List<ProjectMetricEntity> toEntityList(List<ProjectMetricRs> projectMetricRsList);
    List<ProjectMetricRs> toModelList(List<ProjectMetricEntity> projectMetricEntityList);

}
