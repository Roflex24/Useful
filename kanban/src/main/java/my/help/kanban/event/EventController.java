package my.help.kanban.event;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import my.help.kanban.event.dto.EventRs;
import my.help.kanban.event.dto.EventRq;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/event")
@RequiredArgsConstructor
@Tag(name = "Kanban API", description = "Раздел планировая")
public class EventController {

    private final EventService eventService;

    @GetMapping()
    public List<EventRs> getList() {
        return eventService.getList();
    }

    @GetMapping("/{id}")
    public EventRs getById(@PathVariable Long id) {
        return eventService.getById(id);
    }

    @GetMapping("/month")
    public List<EventRs> getByMonth(
            @RequestParam int year,
            @RequestParam int month) {
        return eventService.getByMonth(year, month);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void create(@RequestBody EventRq rq) {
        eventService.create(rq);
    }

    @PutMapping("/{id}")
    public EventRs update(@PathVariable Long id, @RequestBody EventRq rq) {
        return eventService.update(id, rq);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        eventService.delete(id);
    }
}
