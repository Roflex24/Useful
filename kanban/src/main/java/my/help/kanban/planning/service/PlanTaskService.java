package my.help.kanban.planning.service;

import lombok.RequiredArgsConstructor;
import my.help.kanban.planning.dto.PlanTaskRequestDto;
import my.help.kanban.planning.dto.PlanTaskResponseDto;
import my.help.kanban.planning.entity.PlanTask;
import my.help.kanban.planning.entity.StrategicPlan;
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

    @Transactional
    public PlanTaskResponseDto createTask(Long planId, PlanTaskRequestDto dto) {
        StrategicPlan plan = planRepository.findById(planId)
                .orElseThrow(() -> new RuntimeException("Plan not found"));

        PlanTask task = new PlanTask();
        task.setPlan(plan);
        task.setTitle(dto.getTitle());
        task.setDescription(dto.getDescription());
        task.setStatus(dto.getStatus());
        task.setComment(dto.getComment());
        task.setOrderIndex(dto.getOrderIndex() != null ? dto.getOrderIndex() : 0);

        return toResponseDto(taskRepository.save(task));
    }

    @Transactional
    public PlanTaskResponseDto updateTask(Long taskId, PlanTaskRequestDto dto) {
        PlanTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + taskId));

        task.setTitle(dto.getTitle());
        task.setDescription(dto.getDescription());
        task.setStatus(dto.getStatus());
        task.setComment(dto.getComment());
        task.setOrderIndex(dto.getOrderIndex());

        return toResponseDto(taskRepository.save(task));
    }

    public PlanTaskResponseDto getTaskById(Long taskId) {
        PlanTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + taskId));
        return toResponseDto(task);
    }

    public List<PlanTaskResponseDto> getTasksByPlan(Long planId) {
        return taskRepository.findByPlanIdOrderByOrderIndexAsc(planId)
                .stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteTask(Long taskId) {
        if (!taskRepository.existsById(taskId)) {
            throw new RuntimeException("Task not found with id: " + taskId);
        }
        taskRepository.deleteById(taskId);
    }

    @Transactional
    public void deleteAllTasksByPlan(Long planId) {
        StrategicPlan plan = planRepository.findById(planId).orElse(null);
        if (plan != null) {
            taskRepository.deleteByPlan(plan);
        }
    }

    private PlanTaskResponseDto toResponseDto(PlanTask task) {
        PlanTaskResponseDto dto = new PlanTaskResponseDto();
        dto.setId(task.getId());
        dto.setPlanId(task.getPlan().getId());
        dto.setTitle(task.getTitle());
        dto.setDescription(task.getDescription());
        dto.setStatus(task.getStatus());
        dto.setComment(task.getComment());
        dto.setOrderIndex(task.getOrderIndex());
        dto.setCreatedAt(task.getCreatedAt());
        return dto;
    }
}