package my.help.kanban.task;

import my.help.kanban.task.dto.TaskRq;
import my.help.kanban.task.dto.TaskRs;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TaskMapper {

    @Mapping(source = "column.id", target = "columnId")
    TaskRs toModel(TaskEntity entity);

    List<TaskRs> toModelList(List<TaskEntity> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createDate", ignore = true)
    @Mapping(target = "column", ignore = true)
    @Mapping(target = "closeDate", ignore = true)
    TaskEntity toEntity(TaskRq rq);

    List<TaskEntity> toEntityList(List<TaskRs> models);
}