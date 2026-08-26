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
    public PlanResponseDto create(@RequestBody PlanRequestDto rq) {
        return planningService.create(rq);
    }

    @PutMapping("/{id}")
    public PlanResponseDto update(@PathVariable Long id, @RequestBody PlanRequestDto rq) {
        return planningService.update(id, rq);
    }

    @GetMapping("/{id}")
    public PlanResponseDto getById(@PathVariable Long id) {
        return planningService.getById(id);
    }

    @GetMapping
    public List<PlanResponseDto> getPlanList(
            @RequestParam(required = false) String type,
            @RequestParam(required = false, defaultValue = "false") boolean relevantOnly) {
        return planningService.getPlanList(type, relevantOnly);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        planTaskService.deleteByPlan(id);
        planningService.delete(id);
    }

    @GetMapping("/tasks/{taskId}")
    public PlanTaskResponseDto getTaskById(@PathVariable Long taskId) {
        return planTaskService.getById(taskId);
    }

    @PutMapping("/tasks/{taskId}")
    public PlanTaskResponseDto updateTask(@PathVariable Long taskId, @RequestBody PlanTaskRequestDto rq) {
        return planTaskService.update(taskId, rq);
    }

    @DeleteMapping("/tasks/{taskId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTask(@PathVariable Long taskId) {
        planTaskService.delete(taskId);
    }

    @GetMapping("/{planId}/tasks")
    public List<PlanTaskResponseDto> getTasksByPlan(@PathVariable Long planId) {
        return planTaskService.getByPlan(planId);
    }

    @PostMapping("/{planId}/tasks")
    public PlanTaskResponseDto createTask(@PathVariable Long planId, @RequestBody PlanTaskRequestDto rq) {
        return planTaskService.create(planId, rq);
    }
}