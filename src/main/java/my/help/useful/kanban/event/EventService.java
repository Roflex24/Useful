package my.help.useful.kanban.event;

import lombok.RequiredArgsConstructor;
import my.help.useful.kanban.project.ProjectRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventMapper eventMapper;
    private final EventRepository eventRepository;

    public void createEvent(EventRq eventRq) {
        eventRepository.save(eventMapper.rqToEntity(eventRq));
    }

    public List<EventModel> getEventList() {
        return eventMapper.toModelList(eventRepository.findAll());
    }

    public EventModel getEventById(Long id) {
        return eventMapper.toModel(eventRepository.findById(id).get());
    }

    public void updateEvent(EventModel eventModel) {
        eventRepository.save(eventMapper.toEntity(eventModel));
    }

    public void deleteEventById(Long id) {
        eventRepository.deleteById(id);
    }
}
