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

    public void create(EventRq eventRq) {
        eventRepository.save(eventMapper.rqToEntity(eventRq));
    }

    public List<EventModel> getList() {
        return eventMapper.toModelList(eventRepository.findAll());
    }

    public EventModel getById(Long id) {
        return eventMapper.toModel(eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Событие с id=" + id + " не найдено")));
    }

    public List<EventModel> getByMonth(int year, int month) {
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
    public EventModel update(Long id, EventRq rq) {
        EventEntity eventEntity = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Событие с id=" + id + " не найдено"));
        eventEntity.setName(rq.name());
        eventEntity.setDateTime(rq.dateTime());
        eventEntity.setDescription(rq.description());
        return eventMapper.toModel(eventEntity);
    }

    public void delete(Long id) {
        eventRepository.deleteById(id);
    }
}
