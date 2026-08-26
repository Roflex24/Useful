package my.help.kanban.task.dto;

import my.help.kanban.task.Difficulty;

public record TaskRq(
        String title,
        String description,
        int orderIndex,
        Long columnId,
        Difficulty difficulty
) {
}