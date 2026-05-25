package my.help.useful.kanban.task;

import lombok.RequiredArgsConstructor;
import my.help.useful.kanban.column.ColumnEntity;
import my.help.useful.kanban.column.ColumnRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TaskService {

    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;
    private final ColumnRepository columnRepository;

    public List<TaskModel> getTasksByColumn(Long columnId) {
        return taskMapper.toModelList(
                taskRepository.findByColumnId(columnId)
        );
    }

    public void createTask(TaskRq rq) {
        TaskEntity taskEntity = taskMapper.toEntity(rq);
        taskEntity.setColumn(columnRepository.findById(rq.getColumnId()).get());
        taskRepository.save(taskEntity);
    }

    public void updateTaskList(List<TaskModel> rq) {
        List<TaskEntity> taskEntityList = taskMapper.toEntityList(rq);
        for (int i = 0; i < taskEntityList.size(); i++) {
            TaskEntity taskEntity = taskEntityList.get(i);
            taskEntity.setColumn(columnRepository.findById(rq.get(i).getColumnId()).get());
        }
        taskRepository.saveAll(taskEntityList);
    }

    public void deleteTask(Long id) {
        taskRepository.deleteById(id);
    }
}