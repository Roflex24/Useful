package my.help.useful.kanban.task;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    ResponseEntity<Void> updateTask(@RequestBody List<TaskModel> request) {
        taskService.updateTaskList(request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }
}