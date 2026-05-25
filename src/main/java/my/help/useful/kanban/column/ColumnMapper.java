package my.help.useful.kanban.column;

import my.help.useful.kanban.task.TaskMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = TaskMapper.class)
public interface ColumnMapper {

    @Mapping(source = "project.id", target = "projectId")
    ColumnModel toModel(ColumnEntity entity);

    List<ColumnModel> toModelList(List<ColumnEntity> entities);

    ColumnEntity toEntity(ColumnModel model);

    List<ColumnEntity> toEntityList(List<ColumnModel> models);

    ColumnEntity columnRqToEntity(ColumnRq rq);
}