package my.help.useful.kanban.planning.service;

import lombok.RequiredArgsConstructor;
import my.help.useful.kanban.planning.dto.PlanRequestDto;
import my.help.useful.kanban.planning.dto.PlanResponseDto;
import my.help.useful.kanban.planning.entity.StrategicPlan;
import my.help.useful.kanban.planning.repository.StrategicPlanRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlanningService {

    private final StrategicPlanRepository planRepository;

    @Transactional
    public PlanResponseDto createPlan(PlanRequestDto dto) {
        StrategicPlan plan = new StrategicPlan();
        mapDtoToEntity(dto, plan);
        StrategicPlan saved = planRepository.save(plan);
        return toResponseDto(saved);
    }

    @Transactional
    public PlanResponseDto updatePlan(Long id, PlanRequestDto dto) {
        StrategicPlan plan = planRepository.findById(id).orElseThrow();
        mapDtoToEntity(dto, plan);
        return toResponseDto(planRepository.save(plan));
    }

    public PlanResponseDto getPlan(Long id) {
        StrategicPlan plan = planRepository.findById(id).orElseThrow();
        return toResponseDto(plan);
    }

    public List<PlanResponseDto> getAllPlans() {
        return planRepository.findAll().stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deletePlan(Long id) {
        planRepository.deleteById(id);
    }

    private void mapDtoToEntity(PlanRequestDto dto, StrategicPlan plan) {
        plan.setPlanType(dto.getPlanType());
        plan.setTitle(dto.getTitle());
        plan.setStartDate(dto.getStartDate());
        plan.setEndDate(dto.getEndDate());
        plan.setStatus(dto.getStatus());
    }

    private PlanResponseDto toResponseDto(StrategicPlan plan) {
        PlanResponseDto dto = new PlanResponseDto();
        dto.setId(plan.getId());
        dto.setPlanType(plan.getPlanType());
        dto.setTitle(plan.getTitle());
        dto.setStartDate(plan.getStartDate());
        dto.setEndDate(plan.getEndDate());
        dto.setStatus(plan.getStatus());
        return dto;
    }
}