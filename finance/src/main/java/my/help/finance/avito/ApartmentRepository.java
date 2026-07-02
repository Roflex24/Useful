package my.help.finance.avito;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Репозиторий для работы с квартирами.
 * Spring Data JPA автоматически реализует все стандартные методы:
 * save(), findAll(), findById() и т.д.
 */
@Repository
public interface ApartmentRepository extends JpaRepository<Apartment, Long> {

    /** Найти квартиру по avito_id */
    Optional<Apartment> findByAvitoId(String avitoId);

    /** Проверить существование по avito_id */
    boolean existsByAvitoId(String avitoId);
}
