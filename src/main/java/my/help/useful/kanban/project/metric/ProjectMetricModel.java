package my.help.useful.kanban.project.metric;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProjectMetricModel {

    private Long id;
    private String name;
    @JsonProperty("isComplete")
    private boolean isComplete;
    @JsonProperty("isMain")
    private boolean isMain;
    private Integer orderIndex;
    private Long projectId;

    @JsonIgnore
    public boolean isComplete() {
        return isComplete;
    }

    @JsonIgnore
    public boolean isMain() {
        return isMain;
    }
}
