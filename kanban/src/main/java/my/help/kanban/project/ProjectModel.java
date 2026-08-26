package my.help.kanban.project;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import my.help.kanban.metric.ProjectMetricModel;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProjectModel {

    private Long id;
    private String name;
    private String description;
    private LocalDate createDate;

    private ProjectMetricModel mainProjectMetric;
    private Integer mainMetricCompletePercent;

    private boolean archived;
    private LocalDate archiveDate;
}