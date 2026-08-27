package my.help.kanban.task;

import lombok.RequiredArgsConstructor;
import my.help.kanban.column.Column;
import my.help.kanban.column.ColumnRepository;
import my.help.kanban.common.ResourceNotFoundException;
import my.help.kanban.task.dto.TaskRq;
import my.help.kanban.task.dto.TaskRs;
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

    public List<TaskRs> getByColumn(Long columnId) {
        Column column = columnRepository.findById(columnId).orElseThrow(() -> new ResourceNotFoundException(
                String.format("Колонка с id %d не найдена", columnId)));

        List<TaskRs> list = taskMapper.toModelList(
                taskRepository.findByColumnId(columnId));

        if (column.getOrderIndex() == 3) {
            LocalDate limitDate = LocalDate.now().minusDays(DAYS_LIMIT_FOR_ARCHIVE);
            return list.stream()
                    .filter(e -> e.closeDate() == null ||
                            e.closeDate().isAfter(limitDate))
                    .toList();
        } else {
            return list;
        }
    }

    public void create(TaskRq rq) {
        Task task = taskMapper.toEntity(rq);
        if (task.getDifficulty() == null) {
            task.setDifficulty(Difficulty.BASE);
        }
        task.setColumn(columnRepository.findById(rq.columnId())
                .orElseThrow(() -> new ResourceNotFoundException(String.format("Колонка с id %d не найдена", rq.columnId()))));
        taskRepository.save(task);
    }

    public List<TaskRs> updateList(List<TaskRs> rq) {
        List<Task> taskList = taskMapper.toEntityList(rq);
        for (int i = 0; i < taskList.size(); i++) {
            Task task = taskList.get(i);
            Long id = rq.get(i).columnId();
            Column column = columnRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException(String.format("Колонка с id %d не найдена", id)));
            if (column.getOrderIndex() == 3) {
                if (task.getCloseDate() == null) {
                    task.setCloseDate(LocalDate.now());
                }
            } else {
                task.setCloseDate(null);
            }
            task.setColumn(column);
        }
        List<Task> saved = taskRepository.saveAll(taskList);
        return taskMapper.toModelList(saved);
    }

    public void delete(Long id) {
        taskRepository.deleteById(id);
    }

    public long getCountForPeriod(LocalDate start, LocalDate end, Long projectId) {
        List<Task> taskList = taskRepository.findByCloseDateBetweenAndColumnId(
                start, end, columnRepository.findByProjectIdAndOrderIndex(projectId, 3).getId());

        return taskList.size();
    }

    public List<TaskRs> getListForPeriod(LocalDate start, LocalDate end) {
        return taskMapper.toModelList(taskRepository.findByCloseDateBetween(start, end));
    }
}