package my.help.finance.rate.key;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface KeyRateRepository extends JpaRepository<KeyRate, LocalDate> {

    Optional<KeyRate> findById(LocalDate id);

}
