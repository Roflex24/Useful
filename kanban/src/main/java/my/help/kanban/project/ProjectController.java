package my.help.kanban.project;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/project")
public class ProjectController {

    private final ProjectService projectService;

    @GetMapping
    public List<ProjectModel> getProjectList() {
        return projectService.getProjectList();
    }

    @GetMapping("/{id}")
    public ProjectModel getProjectById(@PathVariable Long id) {
        return projectService.getProjectById(id);
    }

    @PostMapping()
    public ProjectModel createProject(@RequestBody ProjectRq rq) {
        return projectService.createProject(rq);
    }

    @PutMapping("/{id}")
    public ProjectModel updateProject(@PathVariable Long id, @RequestBody ProjectRq rq) {
        return projectService.updateProject(id, rq);
    }

    @PutMapping("/{id}/archive")
    public ResponseEntity<Void> archiveProject(@PathVariable Long id) {
        projectService.archiveProject(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/unarchive")
    public ResponseEntity<Void> unarchiveProject(@PathVariable Long id) {
        projectService.unarchiveProject(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProjectById(@PathVariable Long id) {
        projectService.deleteProjectById(id);
    }
}