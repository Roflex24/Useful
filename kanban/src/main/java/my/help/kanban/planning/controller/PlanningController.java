package my.help.kanban.planning.controller;

import lombok.RequiredArgsConstructor;
import my.help.kanban.planning.dto.PlanRequestDto;
import my.help.kanban.planning.dto.PlanResponseDto;
import my.help.kanban.planning.dto.PlanTaskRequestDto;
import my.help.kanban.planning.dto.PlanTaskResponseDto;
import my.help.kanban.planning.service.PlanTaskService;
import my.help.kanban.planning.service.PlanningService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/planning")
@RequiredArgsConstructor
public class PlanningController {

    private final PlanningService planningService;
    private final PlanTaskService planTaskService;

    @PostMapping
    public PlanResponseDto createPlan(@RequestBody PlanRequestDto dto) {
        return planningService.createPlan(dto);
    }

    @PutMapping("/{id}")
    public PlanResponseDto updatePlan(@PathVariable Long id, @RequestBody PlanRequestDto dto) {
        return planningService.updatePlan(id, dto);
    }

    @GetMapping("/{id}")
    public PlanResponseDto getPlan(@PathVariable Long id) {
        return planningService.getPlan(id);
    }

    @GetMapping
    public List<PlanResponseDto> getAllPlans(
            @RequestParam(required = false) String type,
            @RequestParam(required = false, defaultValue = "false") boolean relevantOnly) {
        return planningService.getPlans(type, relevantOnly);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePlan(@PathVariable Long id) {
        planTaskService.deleteAllTasksByPlan(id);
        planningService.deletePlan(id);
    }

    @GetMapping("/tasks/{taskId}")
    public PlanTaskResponseDto getTaskById(@PathVariable Long taskId) {
        return planTaskService.getTaskById(taskId);
    }

    @PutMapping("/tasks/{taskId}")
    public PlanTaskResponseDto updateTask(@PathVariable Long taskId, @RequestBody PlanTaskRequestDto dto) {
        return planTaskService.updateTask(taskId, dto);
    }

    @DeleteMapping("/tasks/{taskId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTask(@PathVariable Long taskId) {
        planTaskService.deleteTask(taskId);
    }

    @GetMapping("/{planId}/tasks")
    public List<PlanTaskResponseDto> getTasksByPlan(@PathVariable Long planId) {
        return planTaskService.getTasksByPlan(planId);
    }

    @PostMapping("/{planId}/tasks")
    public PlanTaskResponseDto createTask(@PathVariable Long planId, @RequestBody PlanTaskRequestDto dto) {
        return planTaskService.createTask(planId, dto);
    }
}