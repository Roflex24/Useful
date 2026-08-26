package my.help.kanban.metric;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/project/metric")
@RequiredArgsConstructor
public class ProjectMetricController {

    private final ProjectMetricService projectMetricService;

    @GetMapping("/{id}")
    public List<ProjectMetricModel> getListByProjectId(@PathVariable Long id) {
        return projectMetricService.getListByProjectId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void create(@RequestBody ProjectMetricRq rq) {
        projectMetricService.create(rq);
    }

    @PutMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateList(@RequestBody List<ProjectMetricModel> rq) {
        projectMetricService.updateList(rq);
    }

    @PutMapping("/{id}")
    public ProjectMetricModel update(@PathVariable Long id, @RequestBody ProjectMetricRq rq) {
        return projectMetricService.update(id, rq);
    }


    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        projectMetricService.delete(id);
    }
}
