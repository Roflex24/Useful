package my.help.kanban.event;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<EventEntity, Long> {
    List<EventEntity> findByDateTimeBetween(LocalDateTime start, LocalDateTime end);
}