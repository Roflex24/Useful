package my.help.finance.invest.bond;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BondDailyDataRepository extends JpaRepository<BondDailyData, Long> {
    List<BondDailyData> findByDate(LocalDate date, Pageable pageable);
}