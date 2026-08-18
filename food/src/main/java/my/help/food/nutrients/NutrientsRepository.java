package my.help.food.nutrients;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface NutrientsRepository extends JpaRepository<NutrientsPerDayEntity, LocalDate> {

    @Query("select n from NutrientsPerDayEntity n where n.date between :start and :end")
    List<NutrientsPerDayEntity> findByDateBetween(@Param("start") LocalDate start, @Param("end") LocalDate end);
}
