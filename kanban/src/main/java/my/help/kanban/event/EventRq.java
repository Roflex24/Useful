package my.help.kanban.event;

import java.time.LocalDateTime;

public record EventRq (
        String name,
        String description,
        LocalDateTime dateTime
) {}
