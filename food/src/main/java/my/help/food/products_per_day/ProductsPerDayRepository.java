package my.help.food.products_per_day;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ProductsPerDayRepository extends JpaRepository<ProductsPerDayEntity, ProductsPerDayKeyEntity> {

    List<ProductsPerDayEntity> findById_Date(LocalDate date);
    void deleteById_Date(LocalDate date);
}