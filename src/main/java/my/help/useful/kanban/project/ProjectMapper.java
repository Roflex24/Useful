package my.help.useful.kanban.project;

import org.mapstruct.Mapper;
import java.util.List;

@Mapper(componentModel = "spring")
public interface ProjectMapper {

    ProjectModel toModel(ProjectEntity projectEntity);
    ProjectEntity toEntity(ProjectModel projectModel);
    List<ProjectModel> toModelList(List<ProjectEntity> projectEntityList);

    ProjectEntity projectRqToEntity(ProjectRq projectRq);

}
