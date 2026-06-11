package my.help.useful.kanban.project;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import java.util.List;

@Mapper(componentModel = "spring")
public interface ProjectMapper {

    ProjectModel toModel(ProjectEntity projectEntity);
    ProjectEntity toEntity(ProjectModel projectModel);
    List<ProjectModel> toModelList(List<ProjectEntity> projectEntityList);

    @Mapping(target = "archived", source = "archived", defaultExpression = "java(false)")
    ProjectEntity projectRqToEntity(ProjectRq projectRq);

}