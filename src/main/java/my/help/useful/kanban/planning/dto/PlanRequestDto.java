package my.help.useful.kanban.planning.dto;

import lombok.Data;
import my.help.useful.kanban.planning.entity.PlanStatus;
import my.help.useful.kanban.planning.entity.PlanType;
import java.time.LocalDate;

@Data
public class PlanRequestDto {
    private PlanType planType;
    private String title;
    private LocalDate startDate;
    private LocalDate endDate;
    private PlanStatus status;
}