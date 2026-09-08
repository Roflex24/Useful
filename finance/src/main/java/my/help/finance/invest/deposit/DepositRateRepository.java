package my.help.finance.invest.deposit;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DepositRateRepository extends JpaRepository<DepositRateEntity, Long> {

    List<DepositRateEntity> findByParseDate(LocalDate parseDate);
}