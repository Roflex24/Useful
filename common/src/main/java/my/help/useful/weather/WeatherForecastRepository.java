package my.help.useful.weather;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface WeatherForecastRepository extends JpaRepository<ForecastItemEntity, Long> {

    // ✅ Удалить все записи старее указанной даты/времени
    @Modifying
    @Transactional
    void deleteAllByIdDateTimeBefore(@Param("dateTime") LocalDateTime dateTime);

    // ✅ Удалить все записи для конкретного города
    @Modifying
    @Transactional
    void deleteByIdCityNameEn(String cityNameEn);

    // ✅ Найти все записи для конкретного города
    List<ForecastItemEntity> findByIdCityNameEn(String cityNameEn);
}
