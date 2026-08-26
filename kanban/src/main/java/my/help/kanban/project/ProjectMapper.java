package my.help.kanban.project;

import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProjectMapper {

    ProjectModel toModel(ProjectEntity projectEntity);

    ProjectEntity projectRqToEntity(ProjectRq projectRq);

}