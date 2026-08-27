package my.help.finance.key_rate;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface KeyRateRepository extends JpaRepository<KeyRateEntity, LocalDate> {

    Optional<KeyRateEntity> findById(LocalDate id);

}
