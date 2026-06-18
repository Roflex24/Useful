package my.help.kanban.task;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaskRq {
    private String title;
    private String description;
    private int orderIndex;
    private Long columnId;
}