package my.help.food.nutrients;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface NutrientsRepository extends JpaRepository<NutrientsPerDayEntity, LocalDate> {

    @Query("""
            select new my.help.food.nutrients.NutrientsWithProductsRowProjection(
                n.date, n.calories, n.protein, n.fat, n.carbohydrates, n.fiber, n.comment,
                p.id, p.name, p.calories, p.protein, p.fat, p.carbohydrate, p.fiber, p.unit, p.photoUrl, p.shop,
                ppd.quantity
            )
            from NutrientsPerDayEntity n
            left join ProductsPerDayEntity ppd on ppd.id.date = n.date
            left join ProductEntity p on p.id = ppd.id.productId
            where n.date = :date
            """)
    List<NutrientsWithProductsRowProjection> findWithProductsByDate(@Param("date") LocalDate date);

    @Query("""
            select new my.help.food.nutrients.NutrientsWithProductsRowProjection(
                n.date, n.calories, n.protein, n.fat, n.carbohydrates, n.fiber, n.comment,
                p.id, p.name, p.calories, p.protein, p.fat, p.carbohydrate, p.fiber, p.unit, p.photoUrl, p.shop,
                ppd.quantity
            )
            from NutrientsPerDayEntity n
            left join ProductsPerDayEntity ppd on ppd.id.date = n.date
            left join ProductEntity p on p.id = ppd.id.productId
            where n.date between :start and :end
            """)
    List<NutrientsWithProductsRowProjection> findWithProductsByDateBetween(
            @Param("start") LocalDate start,
            @Param("end") LocalDate end);
}