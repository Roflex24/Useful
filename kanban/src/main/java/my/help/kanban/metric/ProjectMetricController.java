package my.help.kanban.metric;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/project/metric")
@RequiredArgsConstructor
public class ProjectMetricController {

    private final ProjectMetricService projectMetricService;

    @GetMapping("/{id}")
    public List<ProjectMetricModel> getProjectMetricListByProjectId(@PathVariable Long id) {
        return projectMetricService.getMetricByProjectId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void createProjectMetric(@RequestBody ProjectMetricRq projectMetricRq) {
        projectMetricService.createProjectMetric(projectMetricRq);
    }

    @PutMapping
    public ResponseEntity<Void> updateProjectMetricList(@RequestBody List<ProjectMetricModel> list) {
        projectMetricService.updateProjectMetricList(list);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}")
    public ProjectMetricModel updateProjectMetric(@PathVariable Long id, @RequestBody ProjectMetricRq rq) {
        return projectMetricService.updateProjectMetric(id, rq);
    }


    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProjectMetricById(@PathVariable Long id) {
        projectMetricService.deleteProjectById(id);
    }
}
