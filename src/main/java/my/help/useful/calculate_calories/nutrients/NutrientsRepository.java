package my.help.useful.calculate_calories.nutrients;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface NutrientsRepository extends JpaRepository<NutrientsPerDayEntity, LocalDate> {

}
