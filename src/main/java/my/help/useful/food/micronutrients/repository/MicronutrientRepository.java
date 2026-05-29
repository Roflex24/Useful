package my.help.useful.food.micronutrients.repository;

import my.help.useful.food.micronutrients.model.Micronutrient;
import my.help.useful.food.micronutrients.model.MicronutrientType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface MicronutrientRepository extends JpaRepository<Micronutrient, Long> {

    Optional<Micronutrient> findByName(String name);

    List<Micronutrient> findByType(MicronutrientType type);

    List<Micronutrient> findByTypeOrderByName(MicronutrientType type);

    boolean existsByName(String name);
}