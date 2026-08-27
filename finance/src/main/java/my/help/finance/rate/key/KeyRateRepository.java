package my.help.finance.rate.key;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface KeyRateRepository extends JpaRepository<KeyRate, LocalDate> {
}
