package my.help.kanban.task;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("api/task")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @GetMapping("/column/{columnId}")
    public List<TaskModel> getTasksByColumn(@PathVariable Long columnId) {
        return taskService.getTasksByColumn(columnId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void createTask(@RequestBody TaskRq request) {
        taskService.createTask(request);
    }

    @PutMapping
    public List<TaskModel> updateTask(@RequestBody List<TaskModel> request) {
        return taskService.updateTaskList(request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
    }

    @GetMapping("/period/count")
    public Long getTaskCountForPeriod(@RequestParam LocalDate start, @RequestParam LocalDate end, @RequestParam(required = false) Long projectId) {
        return taskService.getTaskCountForPeriod(start, end, projectId);
    }

    @GetMapping("/period")
    public List<TaskModel> getTaskListForPeriod(@RequestParam LocalDate start, @RequestParam LocalDate end) {
        return taskService.getTaskListForPeriod(start, end);
    }
}