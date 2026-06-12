package my.help.useful.kanban.planning.repository;

import my.help.useful.kanban.planning.entity.PlanType;
import my.help.useful.kanban.planning.entity.StrategicPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface StrategicPlanRepository extends JpaRepository<StrategicPlan, Long> {

    List<StrategicPlan> findByPlanTypeOrderByEndDateDesc(PlanType planType);

    @Query("SELECT p FROM StrategicPlan p ORDER BY p.endDate DESC")
    List<StrategicPlan> findAllOrderByEndDateDesc();

    @Query("SELECT p FROM StrategicPlan p WHERE (:type IS NULL OR p.planType = :type) AND (p.endDate IS NULL OR p.endDate >= :today) ORDER BY p.endDate DESC")
    List<StrategicPlan> findPlansByTypeAndRelevant(@Param("type") PlanType type, @Param("today") LocalDate today);
}