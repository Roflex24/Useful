package my.help.kanban.project.metric;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/project/metric")
@RequiredArgsConstructor
public class ProjectMetricController {

    private final ProjectMetricService projectMetricService;

    @GetMapping("/{id}")
    public ResponseEntity<List<ProjectMetricModel>> getProjectMetricListByProjectId(@PathVariable Long id) {
        return ResponseEntity.ok(projectMetricService.getMetricByProjectId(id));
    }

    @PostMapping
    public ResponseEntity<Void> createProjectMetric(@RequestBody ProjectMetricRq projectMetricRq) {
        projectMetricService.createProjectMetric(projectMetricRq);
        return ResponseEntity.ok().build();
    }

    @PutMapping
    public ResponseEntity<Void> updateProjectMetricList(@RequestBody List<ProjectMetricModel> list) {
        projectMetricService.updateProjectMetricList(list);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updateProjectMetric(@RequestBody ProjectMetricModel rq) {
        projectMetricService.updateProjectMetric(rq);
        return ResponseEntity.ok().build();
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProjectMetricById(@PathVariable Long id) {
        projectMetricService.deleteProjectById(id);
        return ResponseEntity.ok().build();
    }
}
