package my.help.kanban.event;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/event")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @GetMapping()
    public List<EventModel> getList() {
        return eventService.getList();
    }

    @GetMapping("/{id}")
    public EventModel getById(@PathVariable Long id) {
        return eventService.getById(id);
    }

    @GetMapping("/month")
    public List<EventModel> getByMonth(
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
    public EventModel update(@PathVariable Long id, @RequestBody EventRq rq) {
        return eventService.update(id, rq);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        eventService.delete(id);
    }
}
