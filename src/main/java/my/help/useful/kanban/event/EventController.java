package my.help.useful.kanban.event;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/event")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @GetMapping()
    public ResponseEntity<List<EventModel>> getEventList() {
        return ResponseEntity.ok(eventService.getEventList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventModel> getEvent(@PathVariable Long id) {
        return ResponseEntity.ok(eventService.getEventById(id));
    }

    @PostMapping
    public ResponseEntity<Void> createEvent(@RequestBody EventRq eventRq) {
        eventService.createEvent(eventRq);
        return ResponseEntity.ok().build();
    }

    @PutMapping
    public ResponseEntity<Void> updateEvent(@RequestBody EventModel eventModel) {
        eventService.updateEvent(eventModel);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEventById(@PathVariable Long id) {
        eventService.deleteEventById(id);
        return ResponseEntity.ok().build();
    }
}
