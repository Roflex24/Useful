package my.help.kanban.task;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import my.help.kanban.task.dto.TaskRq;
import my.help.kanban.task.dto.TaskRs;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/task")
@RequiredArgsConstructor
@Tag(name = "Kanban API", description = "Раздел планировая")
public class TaskController {

    private final TaskService taskService;

    @GetMapping("/column/{columnId}")
    public List<TaskRs> getByColumn(@PathVariable Long columnId) {
        return taskService.getByColumn(columnId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void create(@RequestBody TaskRq rq) {
        taskService.create(rq);
    }

    @PutMapping
    public List<TaskRs> updateList(@RequestBody List<TaskRs> rq) {
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
    public List<TaskRs> getListForPeriod(@RequestParam LocalDate start, @RequestParam LocalDate end) {
        return taskService.getListForPeriod(start, end);
    }
}