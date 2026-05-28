package my.help.useful.kanban.planning.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "plan_tasks")
public class PlanTask {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "plan_id", nullable = false)
    private StrategicPlan plan;

    private String title;

    @Column(length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    private PlanStatus status = PlanStatus.NOT_STARTED;

    @Column(length = 2000)
    private String comment;

    private Integer orderIndex; // для сортировки задач
}