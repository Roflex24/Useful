package my.help.finance.invest.bond;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface BondDailyDataRepository extends JpaRepository<BondDailyData, Long>, JpaSpecificationExecutor<BondDailyData> {
}