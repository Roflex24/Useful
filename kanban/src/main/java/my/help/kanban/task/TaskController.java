package my.help.kanban.task;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/task")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @GetMapping("/column/{columnId}")
    public List<TaskModel> getByColumn(@PathVariable Long columnId) {
        return taskService.getByColumn(columnId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void create(@RequestBody TaskRq rq) {
        taskService.create(rq);
    }

    @PutMapping
    public List<TaskModel> updateList(@RequestBody List<TaskModel> rq) {
        return taskService.updateList(rq);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        taskService.delete(id);
    }

    @GetMapping("/period/count")
    public Long getCountForPeriod(@RequestParam LocalDate start, @RequestParam LocalDate end, @RequestParam(required = false) Long projectId) {
        return taskService.getCountForPeriod(start, end, projectId);
    }

    @GetMapping("/period")
    public List<TaskModel> getListForPeriod(@RequestParam LocalDate start, @RequestParam LocalDate end) {
        return taskService.getListForPeriod(start, end);
    }
}