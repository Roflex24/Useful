package my.help.food.products_per_day;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface ProductsPerDayRepository extends JpaRepository<ProductsPerDayEntity, ProductsPerDayKeyEntity> {
    void deleteById_Date(LocalDate date);

    boolean existsById_ProductId(Long productId);
}