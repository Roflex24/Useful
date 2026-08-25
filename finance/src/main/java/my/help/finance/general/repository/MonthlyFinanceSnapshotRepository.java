package my.help.finance.general.repository;

import my.help.finance.general.entity.MonthlyFinanceSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MonthlyFinanceSnapshotRepository extends JpaRepository<MonthlyFinanceSnapshot, Long> {

    // Найти все снимки за конкретную дату (все счета)
    List<MonthlyFinanceSnapshot> findBySnapshotDate(LocalDate snapshotDate);

    // Найти все доступные даты снимков (для селектора)
    @Query("SELECT DISTINCT s.snapshotDate FROM MonthlyFinanceSnapshot s ORDER BY s.snapshotDate DESC")
    List<LocalDate> findAllSnapshotDates();

    // Проверить, существует ли снимок за указанный месяц
    boolean existsBySnapshotDate(LocalDate snapshotDate);
}