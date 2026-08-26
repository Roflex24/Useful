package my.help.kanban.planning.dto;

import my.help.kanban.planning.entity.PlanStatus;
import my.help.kanban.planning.entity.PlanType;

import java.time.LocalDate;

public record PlanRs(
        Long id,
        PlanType planType,
        String title,
        String description,
        LocalDate startDate,
        LocalDate endDate,
        PlanStatus status
) {}