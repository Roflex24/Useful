package my.help.kanban.planning.dto;

import lombok.Data;
import my.help.kanban.planning.entity.PlanStatus;

@Data
public class PlanTaskRequestDto {
    private String title;
    private String description;
    private PlanStatus status;
    private String comment;
    private Integer orderIndex;
}