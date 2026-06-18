package my.help.kanban.planning.repository;

import my.help.kanban.planning.entity.PlanTask;
import my.help.kanban.planning.entity.StrategicPlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlanTaskRepository extends JpaRepository<PlanTask, Long> {
    List<PlanTask> findByPlanIdOrderByOrderIndexAsc(Long planId);
    void deleteByPlan(StrategicPlan plan);
}