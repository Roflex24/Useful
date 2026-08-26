package my.help.kanban.event;

import java.time.LocalDateTime;

public record EventModel(
    Long id,
    String name,
    String description,
    LocalDateTime dateTime
) {}
