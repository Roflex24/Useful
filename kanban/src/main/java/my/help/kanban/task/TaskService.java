package my.help.kanban.task;

import lombok.RequiredArgsConstructor;
import my.help.kanban.column.ColumnEntity;
import my.help.kanban.column.ColumnRepository;
import my.help.kanban.common.ResourceNotFoundException;
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
        ColumnEntity column = columnRepository.findById(columnId).orElseThrow(() -> new ResourceNotFoundException(
                String.format("Колонка с id %d не найдена", columnId)));

        List<TaskModel> list = taskMapper.toModelList(
                taskRepository.findByColumnId(columnId));

        if (column.getOrderIndex() == 3) {
            LocalDate limitDate = LocalDate.now().minusDays(DAYS_LIMIT_FOR_ARCHIVE);
            return list.stream()
                    .filter(e -> e.getCloseDate() == null ||
                            e.getCloseDate().isAfter(limitDate))
                    .toList();
        } else {
            return list;
        }
    }

    public void createTask(TaskRq rq) {
        TaskEntity taskEntity = taskMapper.toEntity(rq);
        if (taskEntity.getDifficulty() == null) {
            taskEntity.setDifficulty(Difficulty.BASE);
        }
        taskEntity.setColumn(columnRepository.findById(rq.getColumnId())
                .orElseThrow(() -> new ResourceNotFoundException(String.format("Колонка с id %d не найдена", rq.getColumnId()))));
        taskRepository.save(taskEntity);
    }

    public List<TaskModel> updateTaskList(List<TaskModel> rq) {
        List<TaskEntity> taskEntityList = taskMapper.toEntityList(rq);
        for (int i = 0; i < taskEntityList.size(); i++) {
            TaskEntity taskEntity = taskEntityList.get(i);
            Long id = rq.get(i).getColumnId();
            ColumnEntity column = columnRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException(String.format("Колонка с id %d не найдена", id)));
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

    public long getTaskCountForPeriod(LocalDate start, LocalDate end, Long projectId) {
        List<TaskEntity> taskEntityList = taskRepository.findByCloseDateBetweenAndColumnId(
                start, end, columnRepository.findByProjectIdAndOrderIndex(projectId, 3).getId());

        return taskEntityList.size();
    }

    public List<TaskModel> getTaskListForPeriod(LocalDate start, LocalDate end) {
        return taskMapper.toModelList(taskRepository.findByCloseDateBetween(start, end));
    }
}