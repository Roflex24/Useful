package my.help.kanban.planning.dto;

import my.help.kanban.planning.entity.PlanStatus;

public record PlanTaskRequestDto(
     String title,
     String description,
     PlanStatus status,
     String comment,
     Integer orderIndex
) {}