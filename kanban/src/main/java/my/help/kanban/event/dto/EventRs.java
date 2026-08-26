package my.help.kanban.event.dto;

import java.time.LocalDateTime;

public record EventRs(
    Long id,
    String name,
    String description,
    LocalDateTime dateTime
) {}
