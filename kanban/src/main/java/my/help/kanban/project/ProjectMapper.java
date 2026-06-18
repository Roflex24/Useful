package my.help.kanban.project;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import java.util.List;

@Mapper(componentModel = "spring")
public interface ProjectMapper {

    ProjectModel toModel(ProjectEntity projectEntity);
    ProjectEntity toEntity(ProjectModel projectModel);

    @Mapping(target = "archived", source = "archived", defaultExpression = "java(false)")
    ProjectEntity projectRqToEntity(ProjectRq projectRq);

}