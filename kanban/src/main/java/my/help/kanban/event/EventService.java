package my.help.kanban.event;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
        return eventMapper.toModel(eventRepository.findById(id).get());
    }

    public List<EventModel> getEventsByMonth(int year, int month) {
        // Создаем начало и конец месяца
        LocalDateTime startOfMonth = LocalDateTime.of(year, month, 1, 0, 0, 0);
        LocalDateTime endOfMonth = startOfMonth
                .withDayOfMonth(startOfMonth.toLocalDate().lengthOfMonth())
                .withHour(23)
                .withMinute(59)
                .withSecond(59);

        List<EventEntity> events = eventRepository.findByDateTimeBetween(startOfMonth, endOfMonth);
        return eventMapper.toModelList(events);
    }

    public void updateEvent(EventModel eventModel) {
        eventRepository.save(eventMapper.toEntity(eventModel));
    }

    public void deleteEventById(Long id) {
        eventRepository.deleteById(id);
    }
}
