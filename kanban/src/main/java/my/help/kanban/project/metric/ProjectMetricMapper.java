package my.help.kanban.project.metric;

import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProjectMetricMapper {

    ProjectMetricEntity projectMetricRqToEntity(ProjectMetricRq projectMetricRq);
    ProjectMetricEntity toEntity(ProjectMetricModel projectMetricModel);
    ProjectMetricModel toModel(ProjectMetricEntity projectMetricEntity);
    List<ProjectMetricEntity> toEntityList(List<ProjectMetricModel> projectMetricModelList);
    List<ProjectMetricModel> toModelList(List<ProjectMetricEntity> projectMetricEntityList);

}
