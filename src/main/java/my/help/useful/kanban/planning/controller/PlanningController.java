package my.help.useful.kanban.planning.controller;

import lombok.RequiredArgsConstructor;
import my.help.useful.kanban.planning.dto.PlanRequestDto;
import my.help.useful.kanban.planning.dto.PlanResponseDto;
import my.help.useful.kanban.planning.dto.PlanTaskRequestDto;
import my.help.useful.kanban.planning.dto.PlanTaskResponseDto;
import my.help.useful.kanban.planning.service.PlanTaskService;
import my.help.useful.kanban.planning.service.PlanningService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/planning")
@RequiredArgsConstructor
public class PlanningController {

    private final PlanningService planningService;
    private final PlanTaskService planTaskService;

    @PostMapping
    public ResponseEntity<PlanResponseDto> createPlan(@RequestBody PlanRequestDto dto) {
        return ResponseEntity.ok(planningService.createPlan(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PlanResponseDto> updatePlan(@PathVariable Long id, @RequestBody PlanRequestDto dto) {
        return ResponseEntity.ok(planningService.updatePlan(id, dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlanResponseDto> getPlan(@PathVariable Long id) {
        return ResponseEntity.ok(planningService.getPlan(id));
    }

    @GetMapping
    public ResponseEntity<List<PlanResponseDto>> getAllPlans() {
        return ResponseEntity.ok(planningService.getAllPlans());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePlan(@PathVariable Long id) {
        planTaskService.deleteAllTasksByPlan(id);
        planningService.deletePlan(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/tasks/{taskId}")
    public ResponseEntity<PlanTaskResponseDto> getTaskById(@PathVariable Long taskId) {
        return ResponseEntity.ok(planTaskService.getTaskById(taskId));
    }

    @PutMapping("/tasks/{taskId}")
    public ResponseEntity<PlanTaskResponseDto> updateTask(@PathVariable Long taskId, @RequestBody PlanTaskRequestDto dto) {
        return ResponseEntity.ok(planTaskService.updateTask(taskId, dto));
    }

    @DeleteMapping("/tasks/{taskId}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long taskId) {
        planTaskService.deleteTask(taskId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{planId}/tasks")
    public ResponseEntity<List<PlanTaskResponseDto>> getTasksByPlan(@PathVariable Long planId) {
        return ResponseEntity.ok(planTaskService.getTasksByPlan(planId));
    }

    @PostMapping("/{planId}/tasks")
    public ResponseEntity<PlanTaskResponseDto> createTask(@PathVariable Long planId, @RequestBody PlanTaskRequestDto dto) {
        return ResponseEntity.ok(planTaskService.createTask(planId, dto));
    }
}