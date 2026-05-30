package my.help.useful.kanban.task;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @GetMapping("/column/{columnId}")
    ResponseEntity<List<TaskModel>> getTasksByColumn(@PathVariable Long columnId) {
        return ResponseEntity.ok(taskService.getTasksByColumn(columnId));
    }

    @PostMapping
    ResponseEntity<Void> createTask(@RequestBody TaskRq request) {
        taskService.createTask(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping
    ResponseEntity<List<TaskModel>> updateTask(@RequestBody List<TaskModel> request) {
        return ResponseEntity.ok(taskService.updateTaskList(request));
    }

    @DeleteMapping("/{id}")
    ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/period/count")
    ResponseEntity<Long> getTaskCountForPeriod(@RequestParam LocalDate start, @RequestParam LocalDate end, @RequestParam(required = false) Long projectId) {
        return ResponseEntity.ok(taskService.getTaskCountForPeriod(start, end, projectId));
    }

    @GetMapping("/period")
    ResponseEntity<List<TaskModel>> getTaskListForPeriod(@RequestParam LocalDate start, @RequestParam LocalDate end) {
        return ResponseEntity.ok(taskService.getTaskListForPeriod(start, end));
    }
}