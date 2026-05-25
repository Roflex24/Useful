package my.help.useful.exchange_rate;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface CurrencyRateRepository extends JpaRepository<CurrencyRateEntity, Long> {

    Optional<CurrencyRateEntity> findByActualDate(LocalDate actualDate);
}
