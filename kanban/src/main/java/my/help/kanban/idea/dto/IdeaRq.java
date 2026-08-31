package my.help.kanban.idea.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import my.help.kanban.idea.enums.IdeaPriority;
import my.help.kanban.idea.enums.IdeaStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class IdeaRq {

    @NotBlank(message = "Название идеи не может быть пустым")
    private String title;

    private String description;

    private List<String> tags = new ArrayList<>();

    private IdeaPriority priority = IdeaPriority.MEDIUM;

    private IdeaStatus status = IdeaStatus.NEW;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}