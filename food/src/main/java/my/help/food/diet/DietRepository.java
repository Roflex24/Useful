package my.help.food.diet;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DietRepository extends JpaRepository<DietEntity, Long> {

    @Query("select distinct d from DietEntity d left join fetch d.items order by d.name")
    List<DietEntity> findAllWithItems();

    boolean existsByItemsProductId(Long productId);
}