package my.help.kanban.metric;

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
    ProjectMetricEntity toEntity(ProjectMetricModel projectMetricModel);

    @Mapping(target = "projectId", ignore = true)
    ProjectMetricModel toModel(ProjectMetricEntity projectMetricEntity);

    List<ProjectMetricEntity> toEntityList(List<ProjectMetricModel> projectMetricModelList);
    List<ProjectMetricModel> toModelList(List<ProjectMetricEntity> projectMetricEntityList);

}
