package my.help.kanban.planning.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import my.help.kanban.planning.dto.PlanRq;
import my.help.kanban.planning.dto.PlanRs;
import my.help.kanban.planning.dto.PlanTaskRq;
import my.help.kanban.planning.dto.PlanTaskRs;
import my.help.kanban.planning.service.PlanTaskService;
import my.help.kanban.planning.service.PlanningService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/planning")
@RequiredArgsConstructor
@Tag(name = "Kanban API", description = "Раздел планировая")
public class PlanningController {

    private final PlanningService planningService;
    private final PlanTaskService planTaskService;

    @PostMapping
    public PlanRs create(@RequestBody PlanRq rq) {
        return planningService.create(rq);
    }

    @PutMapping("/{id}")
    public PlanRs update(@PathVariable Long id, @RequestBody PlanRq rq) {
        return planningService.update(id, rq);
    }

    @GetMapping("/{id}")
    public PlanRs getById(@PathVariable Long id) {
        return planningService.getById(id);
    }

    @GetMapping
    public List<PlanRs> getPlanList(
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
    public PlanTaskRs getTaskById(@PathVariable Long taskId) {
        return planTaskService.getById(taskId);
    }

    @PutMapping("/tasks/{taskId}")
    public PlanTaskRs updateTask(@PathVariable Long taskId, @RequestBody PlanTaskRq rq) {
        return planTaskService.update(taskId, rq);
    }

    @DeleteMapping("/tasks/{taskId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTask(@PathVariable Long taskId) {
        planTaskService.delete(taskId);
    }

    @GetMapping("/{planId}/tasks")
    public List<PlanTaskRs> getTasksByPlan(@PathVariable Long planId) {
        return planTaskService.getByPlan(planId);
    }

    @PostMapping("/{planId}/tasks")
    public PlanTaskRs createTask(@PathVariable Long planId, @RequestBody PlanTaskRq rq) {
        return planTaskService.create(planId, rq);
    }
}