package my.help.kanban.project;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProjectMapper {

    @Mapping(target = "mainProjectMetric", ignore = true)
    @Mapping(target = "mainMetricCompletePercent", ignore = true)
    ProjectModel toModel(ProjectEntity projectEntity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createDate", ignore = true)
    @Mapping(target = "archived", ignore = true)
    @Mapping(target = "archiveDate", ignore = true)
    ProjectEntity projectRqToEntity(ProjectRq projectRq);

}