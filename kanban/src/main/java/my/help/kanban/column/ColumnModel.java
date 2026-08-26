package my.help.kanban.column;

public record ColumnModel(
    Long id,
    String title,
    int orderIndex,
    Long projectId
){}