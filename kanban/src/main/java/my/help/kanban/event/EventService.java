package my.help.kanban.event;

import lombok.RequiredArgsConstructor;
import my.help.kanban.common.ResourceNotFoundException;
import my.help.kanban.event.dto.EventRs;
import my.help.kanban.event.dto.EventRq;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventMapper eventMapper;
    private final EventRepository eventRepository;

    public void create(EventRq eventRq) {
        eventRepository.save(eventMapper.rqToEntity(eventRq));
    }

    public List<EventRs> getList() {
        return eventMapper.toModelList(eventRepository.findAll());
    }

    public EventRs getById(Long id) {
        return eventMapper.toModel(eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Событие с id=" + id + " не найдено")));
    }

    public List<EventRs> getByMonth(int year, int month) {
        LocalDateTime startOfMonth = LocalDateTime.of(year, month, 1, 0, 0, 0);
        LocalDateTime endOfMonth = startOfMonth
                .withDayOfMonth(startOfMonth.toLocalDate().lengthOfMonth())
                .withHour(23)
                .withMinute(59)
                .withSecond(59);

        List<Event> events = eventRepository.findByDateTimeBetween(startOfMonth, endOfMonth);
        return eventMapper.toModelList(events);
    }

    @Transactional
    public EventRs update(Long id, EventRq rq) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Событие с id=" + id + " не найдено"));
        event.setName(rq.name());
        event.setDateTime(rq.dateTime());
        event.setDescription(rq.description());
        return eventMapper.toModel(event);
    }

    public void delete(Long id) {
        eventRepository.deleteById(id);
    }

    public String getEventsForTelegram() {
        LocalDateTime start = LocalDateTime.now()
                .toLocalDate()
                .atStartOfDay();          // сегодня 00:00:00
        LocalDateTime end = start.plusDays(7); // через 7 дней 00:00:00 (не включая)

        List<Event> events = eventRepository
                .findByDateTimeGreaterThanEqualAndDateTimeLessThanOrderByDateTimeAsc(start, end);

        if (events.isEmpty()) {
            return "Предстоящих событий на ближайшие 7 дней нет.";
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        StringBuilder sb = new StringBuilder("📅 События на ближайшие 7 дней:\n\n");

        for (Event event : events) {
            sb.append("• ")
                    .append(event.getDateTime().format(formatter))
                    .append(" — ")
                    .append(event.getName());

            if (event.getDescription() != null && !event.getDescription().isBlank()) {
                sb.append("\n   ").append(event.getDescription());
            }
            sb.append("\n\n");
        }

        return sb.toString().trim();
    }
}
