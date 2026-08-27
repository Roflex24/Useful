package my.help.kanban.task;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    void deleteByColumnId(Long columnId);
    List<Task> findByColumnId(Long columnId);
    List<Task> findByCloseDateBetweenAndColumnId(LocalDate closeDate, LocalDate closeDate2, Long columnId);
    List<Task> findByCloseDateBetween(LocalDate closeDate, LocalDate closeDate2);
}