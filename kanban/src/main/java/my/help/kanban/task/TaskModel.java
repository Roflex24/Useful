package my.help.kanban.task;

import java.time.LocalDate;

public record TaskModel(
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