package my.help.kanban.project.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import my.help.kanban.metric.dto.ProjectMetricRs;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProjectRs {

    private Long id;
    private String name;
    private String description;
    private LocalDate createDate;

    private ProjectMetricRs mainProjectMetric;
    private Integer mainMetricCompletePercent;

    private boolean archived;
    private LocalDate archiveDate;
}