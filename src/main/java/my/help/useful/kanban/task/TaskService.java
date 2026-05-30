package my.help.useful.kanban.task;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import my.help.useful.kanban.column.ColumnEntity;
import my.help.useful.kanban.column.ColumnRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TaskService {

    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;
    private final ColumnRepository columnRepository;

    private static final int DAYS_LIMIT_FOR_ARCHIVE = 7;

    public List<TaskModel> getTasksByColumn(Long columnId) {
        ColumnEntity column = columnRepository.findById(columnId).orElseThrow(() -> new EntityNotFoundException(
                String.format("Column with id %d not found", columnId)));

        List<TaskModel> list = taskMapper.toModelList(
                taskRepository.findByColumnId(columnId));

        if (column.getOrderIndex() == 3) {
            return list.stream()
                    .filter(e -> e.getCloseDate().isAfter(LocalDate.now().minusDays(DAYS_LIMIT_FOR_ARCHIVE)))
                    .toList();
        } else {
            return list;
        }
    }

    public void createTask(TaskRq rq) {
        TaskEntity taskEntity = taskMapper.toEntity(rq);
        taskEntity.setColumn(columnRepository.findById(rq.getColumnId()).get());
        taskRepository.save(taskEntity);
    }

    public List<TaskModel> updateTaskList(List<TaskModel> rq) {
        List<TaskEntity> taskEntityList = taskMapper.toEntityList(rq);
        for (int i = 0; i < taskEntityList.size(); i++) {
            TaskEntity taskEntity = taskEntityList.get(i);
            ColumnEntity column = columnRepository.findById(rq.get(i).getColumnId()).get();
            if (column.getOrderIndex() == 3) {
                if (taskEntity.getCloseDate() == null) {
                    taskEntity.setCloseDate(LocalDate.now());
                }
            } else {
                taskEntity.setCloseDate(null);
            }
            taskEntity.setColumn(column);
        }
        List<TaskEntity> saved = taskRepository.saveAll(taskEntityList);
        return taskMapper.toModelList(saved);
    }

    public void deleteTask(Long id) {
        taskRepository.deleteById(id);
    }

    public long getTaskListForPeriod(LocalDate start, LocalDate end, Long projectId) {
        List<TaskEntity> taskEntityList = taskRepository.findByCloseDateBetweenAndColumnId(
                start, end, columnRepository.findByProjectIdAndOrderIndex(projectId, 3).getId());

        return taskEntityList.size();
    }
}