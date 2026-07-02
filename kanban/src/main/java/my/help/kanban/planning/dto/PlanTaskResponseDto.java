package my.help.kanban.planning.dto;

import lombok.Data;
import my.help.kanban.planning.entity.PlanStatus;

import java.time.LocalDateTime;

@Data
public class PlanTaskResponseDto {
    private Long id;
    private Long planId;
    private String title;
    private String description;
    private PlanStatus status;
    private String comment;
    private Integer orderIndex;
    private LocalDateTime createdAt;
}