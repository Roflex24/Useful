package my.help.kanban.idea.dto;

import my.help.kanban.idea.enums.IdeaPriority;
import my.help.kanban.idea.enums.IdeaStatus;

import java.time.LocalDateTime;
import java.util.List;

public record IdeaRs(
        Long id,
        String title,
        String description,
        List<String> tags,
        IdeaPriority priority,
        IdeaStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}