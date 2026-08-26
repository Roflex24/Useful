package my.help.kanban.task.dto;

import my.help.kanban.task.Difficulty;

import java.time.LocalDate;

public record TaskRs(
        Long id,
        String title,
        String description,
        int orderIndex,
        Difficulty difficulty,
        LocalDate createDate,
        LocalDate closeDate,
        Long columnId
) {
}