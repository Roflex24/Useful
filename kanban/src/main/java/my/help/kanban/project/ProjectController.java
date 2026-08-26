package my.help.kanban.project;

import lombok.RequiredArgsConstructor;
import my.help.kanban.project.dto.ProjectRs;
import my.help.kanban.project.dto.ProjectRq;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/project")
public class ProjectController {

    private final ProjectService projectService;

    @GetMapping
    public List<ProjectRs> getList() {
        return projectService.getList();
    }

    @GetMapping("/{id}")
    public ProjectRs getById(@PathVariable Long id) {
        return projectService.getProjectById(id);
    }

    @PostMapping()
    public ProjectRs create(@RequestBody ProjectRq rq) {
        return projectService.create(rq);
    }

    @PutMapping("/{id}")
    public ProjectRs update(@PathVariable Long id, @RequestBody ProjectRq rq) {
        return projectService.update(id, rq);
    }

    @PutMapping("/{id}/archive")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void archiveProject(@PathVariable Long id) {
        projectService.archiveProject(id);
    }

    @PutMapping("/{id}/unarchive")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unarchiveProject(@PathVariable Long id) {
        projectService.unarchiveProject(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        projectService.delete(id);
    }
}