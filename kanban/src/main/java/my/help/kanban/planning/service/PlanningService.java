package my.help.kanban.planning.service;

import lombok.RequiredArgsConstructor;
import my.help.kanban.common.ResourceNotFoundException;
import my.help.kanban.planning.dto.PlanRq;
import my.help.kanban.planning.dto.PlanRs;
import my.help.kanban.planning.entity.PlanType;
import my.help.kanban.planning.entity.StrategicPlan;
import my.help.kanban.planning.mapper.PlanningMapper;
import my.help.kanban.planning.repository.StrategicPlanRepository;
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
    public PlanRs create(PlanRq rq) {
        StrategicPlan saved = planRepository.save(planningMapper.rqToEntity(rq));
        return planningMapper.toResponseDto(saved);
    }

    @Transactional
    public PlanRs update(Long id, PlanRq rq) {
        StrategicPlan plan = planRepository.findById(id).orElseThrow();
        planningMapper.updateEntity(rq, plan);
        return planningMapper.toResponseDto(planRepository.save(plan));
    }

    public PlanRs getById(Long id) {
        StrategicPlan plan = planRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("План с id=" + id + " не найден"));
        return planningMapper.toResponseDto(plan);
    }

    public List<PlanRs> getPlanList(String type, boolean relevantOnly) {
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
    public void delete(Long id) {
        planRepository.deleteById(id);
    }
}