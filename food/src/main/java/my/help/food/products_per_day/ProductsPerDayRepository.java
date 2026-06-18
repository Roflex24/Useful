package my.help.food.products_per_day;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;


@Repository
public interface ProductsPerDayRepository extends JpaRepository<ProductsPerDayEntity, Long> {

    List<ProductsPerDayEntity> findByIdDate(LocalDate date);
    void deleteByIdDate(LocalDate date);
}