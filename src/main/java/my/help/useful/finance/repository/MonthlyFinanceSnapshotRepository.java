// repository/MonthlyFinanceSnapshotRepository.java
package my.help.useful.finance.repository;

import my.help.useful.finance.entity.MonthlyFinanceSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface MonthlyFinanceSnapshotRepository extends JpaRepository<MonthlyFinanceSnapshot, Long> {

    // Найти все снимки за конкретную дату (все счета)
    List<MonthlyFinanceSnapshot> findBySnapshotDate(LocalDate snapshotDate);

    // Найти снимок конкретного счёта за конкретную дату
    Optional<MonthlyFinanceSnapshot> findBySnapshotDateAndAccountId(LocalDate snapshotDate, Long accountId);

    // Найти последний доступный снимок (любой) - для проверки наличия данных
    @Query("SELECT MAX(s.snapshotDate) FROM MonthlyFinanceSnapshot s")
    Optional<LocalDate> findLatestSnapshotDate();

    // Найти все доступные даты снимков (для селектора)
    @Query("SELECT DISTINCT s.snapshotDate FROM MonthlyFinanceSnapshot s ORDER BY s.snapshotDate DESC")
    List<LocalDate> findAllSnapshotDates();

    // Проверить, существует ли снимок за указанный месяц
    boolean existsBySnapshotDate(LocalDate snapshotDate);

    // Удалить старые снимки (например, старше 3 лет)
    void deleteBySnapshotDateBefore(LocalDate date);
}