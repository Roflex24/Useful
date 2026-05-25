package my.help.useful.vacancy;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface VacancyRepository extends JpaRepository<VacancyEntity, Long> {

    Optional<VacancyEntity> findByDate(LocalDate date);
}
