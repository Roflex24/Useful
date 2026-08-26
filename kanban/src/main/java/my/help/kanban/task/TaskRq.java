package my.help.kanban.task;

public record TaskRq(
        String title,
        String description,
        int orderIndex,
        Long columnId,
        Difficulty difficulty
) {
}