package my.help.kanban.event;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/event")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @GetMapping()
    public List<EventModel> getEventList() {
        return eventService.getEventList();
    }

    @GetMapping("/{id}")
    public EventModel getEvent(@PathVariable Long id) {
        return eventService.getEventById(id);
    }

    @GetMapping("/month")
    public List<EventModel> getEventsByMonth(
            @RequestParam int year,
            @RequestParam int month) {
        return eventService.getEventsByMonth(year, month);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void createEvent(@RequestBody EventRq eventRq) {
        eventService.createEvent(eventRq);
    }

    @PutMapping
    public ResponseEntity<Void> updateEvent(@RequestBody EventModel eventModel) {
        eventService.updateEvent(eventModel);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteEventById(@PathVariable Long id) {
        eventService.deleteEventById(id);
    }
}
