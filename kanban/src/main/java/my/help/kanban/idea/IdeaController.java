package my.help.kanban.idea;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import my.help.kanban.idea.dto.IdeaRq;
import my.help.kanban.idea.dto.IdeaRs;
import my.help.kanban.idea.enums.IdeaStatus;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ideas")
@RequiredArgsConstructor
public class IdeaController {

    private final IdeaService ideaService;

    @GetMapping
    public List<IdeaRs> getAllIdeas(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "desc") String sortDirection) {
        return ideaService.getIdeas(status, priority, search, sortDirection);
    }

    @GetMapping("/{id}")
    public IdeaRs getIdea(@PathVariable Long id) {
        return ideaService.getIdea(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public IdeaRs createIdea(@Valid @RequestBody IdeaRq ideaRq) {
        return ideaService.createIdea(ideaRq);
    }

    @PutMapping("/{id}")
    public IdeaRs updateIdea(@PathVariable Long id, @Valid @RequestBody IdeaRq ideaRq) {
        return ideaService.updateIdea(id, ideaRq);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteIdea(@PathVariable Long id) {
        ideaService.deleteIdea(id);
    }

    @PatchMapping("/{id}/status")
    public IdeaRs updateStatus(@PathVariable Long id, @RequestParam String status) {
        return ideaService.updateStatus(id, IdeaStatus.fromValue(status));
    }
}