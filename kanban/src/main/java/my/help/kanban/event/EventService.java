package my.help.kanban.event;

import lombok.RequiredArgsConstructor;
import my.help.kanban.common.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
        return eventMapper.toModel(eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Событие с id=" + id + " не найдено")));
    }

    public List<EventModel> getEventsByMonth(int year, int month) {
        LocalDateTime startOfMonth = LocalDateTime.of(year, month, 1, 0, 0, 0);
        LocalDateTime endOfMonth = startOfMonth
                .withDayOfMonth(startOfMonth.toLocalDate().lengthOfMonth())
                .withHour(23)
                .withMinute(59)
                .withSecond(59);

        List<EventEntity> events = eventRepository.findByDateTimeBetween(startOfMonth, endOfMonth);
        return eventMapper.toModelList(events);
    }

    @Transactional
    public EventModel updateEvent(Long id, EventRq rq) {
        EventEntity eventEntity = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Событие с id=" + id + " не найдено"));
        eventEntity.setName(rq.getName());
        eventEntity.setDateTime(rq.getDateTime());
        eventEntity.setDescription(rq.getDescription());
        return eventMapper.toModel(eventEntity);
    }

    public void deleteEventById(Long id) {
        eventRepository.deleteById(id);
    }
}
