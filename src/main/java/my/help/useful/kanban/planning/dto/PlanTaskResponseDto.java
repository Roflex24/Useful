package my.help.useful.kanban.planning.dto;

import lombok.Data;
import my.help.useful.kanban.planning.entity.PlanStatus;

@Data
public class PlanTaskResponseDto {
    private Long id;
    private Long planId;
    private String title;
    private String description;
    private PlanStatus status;
    private String comment;
    private Integer orderIndex;
}