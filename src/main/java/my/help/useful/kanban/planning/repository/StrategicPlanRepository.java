package my.help.useful.kanban.planning.repository;

import my.help.useful.kanban.planning.entity.StrategicPlan;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StrategicPlanRepository extends JpaRepository<StrategicPlan, Long> {
}