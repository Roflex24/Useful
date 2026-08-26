package my.help.kanban.metric;

public record ProjectMetricRq (
        String name,
        boolean isComplete,
        boolean isMain,
        Integer orderIndex,
        Long projectId
) {}
