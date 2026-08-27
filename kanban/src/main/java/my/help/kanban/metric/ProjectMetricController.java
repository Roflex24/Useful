package my.help.kanban.metric;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import my.help.kanban.metric.dto.ProjectMetricRs;
import my.help.kanban.metric.dto.ProjectMetricRq;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/project/metric")
@RequiredArgsConstructor
@Tag(name = "Kanban API", description = "Раздел планировая")
public class ProjectMetricController {

    private final ProjectMetricService projectMetricService;

    @GetMapping("/{id}")
    public List<ProjectMetricRs> getListByProjectId(@PathVariable Long id) {
        return projectMetricService.getListByProjectId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void create(@RequestBody ProjectMetricRq rq) {
        projectMetricService.create(rq);
    }

    @PutMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateList(@RequestBody List<ProjectMetricRs> rq) {
        projectMetricService.updateList(rq);
    }

    @PutMapping("/{id}")
    public ProjectMetricRs update(@PathVariable Long id, @RequestBody ProjectMetricRq rq) {
        return projectMetricService.update(id, rq);
    }


    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        projectMetricService.delete(id);
    }
}
