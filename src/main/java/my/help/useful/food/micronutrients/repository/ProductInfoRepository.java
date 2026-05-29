package my.help.useful.food.micronutrients.repository;

import my.help.useful.food.micronutrients.model.ProductInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductInfoRepository extends JpaRepository<ProductInfo, Long> {

    List<ProductInfo> findByCategory(String category);

    List<ProductInfo> findByNameContainingIgnoreCase(String name);

    @Query("SELECT p FROM ProductInfo p WHERE SIZE(p.micronutrients) > 0")
    List<ProductInfo> findProductsWithMicronutrients();

    @Query("SELECT p FROM ProductInfo p WHERE KEY(p.micronutrients) = :micronutrientName")
    List<ProductInfo> findByMicronutrientName(@Param("micronutrientName") String micronutrientName);
}