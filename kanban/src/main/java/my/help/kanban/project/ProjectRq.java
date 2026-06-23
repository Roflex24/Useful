package my.help.kanban.project;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProjectRq {

    private String name;
    private String description;
    private boolean archived;
}