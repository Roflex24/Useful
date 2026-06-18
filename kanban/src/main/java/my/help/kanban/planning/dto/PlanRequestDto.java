package my.help.kanban.planning.dto;

import lombok.Data;
import my.help.kanban.planning.entity.PlanStatus;
import my.help.kanban.planning.entity.PlanType;
import java.time.LocalDate;

@Data
public class PlanRequestDto {
    private PlanType planType;
    private String title;
    private LocalDate startDate;
    private LocalDate endDate;
    private PlanStatus status;
}