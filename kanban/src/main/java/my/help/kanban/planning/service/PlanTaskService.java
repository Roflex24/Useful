package my.help.kanban.planning.service;

import lombok.RequiredArgsConstructor;
import my.help.kanban.planning.dto.PlanTaskRq;
import my.help.kanban.planning.dto.PlanTaskRs;
import my.help.kanban.planning.entity.PlanTask;
import my.help.kanban.planning.entity.StrategicPlan;
import my.help.kanban.planning.mapper.PlanTaskMapper;
import my.help.kanban.planning.repository.PlanTaskRepository;
import my.help.kanban.planning.repository.StrategicPlanRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlanTaskService {

    private final PlanTaskRepository taskRepository;
    private final StrategicPlanRepository planRepository;
    private final PlanTaskMapper planTaskMapper;

    @Transactional
    public PlanTaskRs create(Long planId, PlanTaskRq rq) {
        StrategicPlan plan = planRepository.findById(planId)
                .orElseThrow(() -> new RuntimeException("Plan not found"));

        PlanTask task = new PlanTask();
        task.setPlan(plan);
        task.setTitle(rq.title());
        task.setDescription(rq.description());
        task.setStatus(rq.status());
        task.setComment(rq.comment());
        task.setOrderIndex(rq.orderIndex() != null ? rq.orderIndex() : 0);

        return planTaskMapper.toResponseDto(taskRepository.save(task));
    }

    @Transactional
    public PlanTaskRs update(Long taskId, PlanTaskRq dto) {
        PlanTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + taskId));

        task.setTitle(dto.title());
        task.setDescription(dto.description());
        task.setStatus(dto.status());
        task.setComment(dto.comment());
        task.setOrderIndex(dto.orderIndex());

        return planTaskMapper.toResponseDto(taskRepository.save(task));
    }

    public PlanTaskRs getById(Long taskId) {
        PlanTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + taskId));
        return planTaskMapper.toResponseDto(task);
    }

    public List<PlanTaskRs> getByPlan(Long planId) {
        return taskRepository.findByPlanIdOrderByOrderIndexAsc(planId)
                .stream()
                .map(planTaskMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void delete(Long taskId) {
        if (!taskRepository.existsById(taskId)) {
            throw new RuntimeException("Task not found with id: " + taskId);
        }
        taskRepository.deleteById(taskId);
    }

    @Transactional
    public void deleteByPlan(Long planId) {
        planRepository.findById(planId).ifPresent(taskRepository::deleteByPlan);
    }
}