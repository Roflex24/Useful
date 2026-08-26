package my.help.kanban.planning.dto;

import my.help.kanban.planning.entity.PlanStatus;

import java.time.LocalDateTime;

public record PlanTaskRs(
         Long id,
         Long planId,
         String title,
         String description,
         PlanStatus status,
         String comment,
         Integer orderIndex,
         LocalDateTime createdAt
) {}