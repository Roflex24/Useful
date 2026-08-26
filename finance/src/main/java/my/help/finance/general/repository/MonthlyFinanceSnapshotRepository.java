package my.help.finance.general.repository;

import my.help.finance.general.entity.MonthlyFinanceSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MonthlyFinanceSnapshotRepository extends JpaRepository<MonthlyFinanceSnapshot, Long> {

    List<MonthlyFinanceSnapshot> findBySnapshotDate(LocalDate snapshotDate);

    @Query("SELECT DISTINCT s.snapshotDate FROM MonthlyFinanceSnapshot s ORDER BY s.snapshotDate DESC")
    List<LocalDate> findAllSnapshotDates();

    boolean existsBySnapshotDate(LocalDate snapshotDate);
}