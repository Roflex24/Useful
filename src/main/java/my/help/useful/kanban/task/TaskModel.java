package my.help.useful.kanban.task;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaskModel {
    private Long id;
    private String title;
    private String description;
    private int orderIndex;
    private Difficulty difficulty;
    private LocalDate createDate;
    private Long columnId;
}