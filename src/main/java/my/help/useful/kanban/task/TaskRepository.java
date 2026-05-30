package my.help.useful.kanban.task;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<TaskEntity, Long> {
    void deleteByColumnId(Long columnId);
    List<TaskEntity> findByColumnId(Long columnId);
    List<TaskEntity> findByCloseDateBetweenAndColumnId(LocalDate closeDate, LocalDate closeDate2, Long columnId);
    List<TaskEntity> findByCloseDateBetween(LocalDate closeDate, LocalDate closeDate2);
}