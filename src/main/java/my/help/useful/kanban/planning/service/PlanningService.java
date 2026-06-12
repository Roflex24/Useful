package my.help.useful.kanban.planning.service;

import lombok.RequiredArgsConstructor;
import my.help.useful.kanban.planning.dto.PlanRequestDto;
import my.help.useful.kanban.planning.dto.PlanResponseDto;
import my.help.useful.kanban.planning.entity.PlanType;
import my.help.useful.kanban.planning.entity.StrategicPlan;
import my.help.useful.kanban.planning.mapper.PlanningMapper;
import my.help.useful.kanban.planning.repository.StrategicPlanRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PlanningService {

    private final StrategicPlanRepository planRepository;
    private final PlanningMapper planningMapper;

    @Transactional
    public PlanResponseDto createPlan(PlanRequestDto dto) {
        StrategicPlan saved = planRepository.save(planningMapper.rqToEntity(dto));
        return planningMapper.toResponseDto(saved);
    }

    @Transactional
    public PlanResponseDto updatePlan(Long id, PlanRequestDto dto) {
        StrategicPlan plan = planRepository.findById(id).orElseThrow();
        planningMapper.updateEntity(dto, plan);
        return planningMapper.toResponseDto(planRepository.save(plan));
    }

    public PlanResponseDto getPlan(Long id) {
        StrategicPlan plan = planRepository.findById(id).orElseThrow();
        return planningMapper.toResponseDto(plan);
    }

    public List<PlanResponseDto> getPlans(String type, boolean relevantOnly) {
        LocalDate today = LocalDate.now();
        PlanType planType = null;

        if (type != null && !type.isEmpty()) {
            planType = PlanType.valueOf(type);
        }

        List<StrategicPlan> plans;
        if (relevantOnly) {
            plans = planRepository.findPlansByTypeAndRelevant(planType, today);
        } else {
            if (planType != null) {
                plans = planRepository.findByPlanTypeOrderByEndDateDesc(planType);
            } else {
                plans = planRepository.findAllOrderByEndDateDesc();
            }
        }

        return planningMapper.toResponseDtoList(plans);
    }

    @Transactional
    public void deletePlan(Long id) {
        planRepository.deleteById(id);
    }
}