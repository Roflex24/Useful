package my.help.kanban.project.metric;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProjectMetricRq {

    private String name;
    private boolean isComplete;
    private boolean isMain;
    private Integer orderIndex;
    private Long projectId;
}
