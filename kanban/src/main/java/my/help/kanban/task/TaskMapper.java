package my.help.kanban.task;

import my.help.kanban.task.dto.TaskRq;
import my.help.kanban.task.dto.TaskRs;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TaskMapper {

    @Mapping(source = "column.id", target = "columnId")
    TaskRs toModel(Task entity);

    List<TaskRs> toModelList(List<Task> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createDate", ignore = true)
    @Mapping(target = "column", ignore = true)
    @Mapping(target = "closeDate", ignore = true)
    Task toEntity(TaskRq rq);

    List<Task> toEntityList(List<TaskRs> models);
}