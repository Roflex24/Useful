package my.help.finance.avito;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для работы с квартирами.
 * Spring Data JPA автоматически реализует все стандартные методы:
 * save(), findAll(), findById() и т.д.
 *
 * findAll() и findByAvitoId() переопределены с @EntityGraph, чтобы
 * коллекции images/badges (LAZY) подтягивались сразу, а не по одному
 * запросу на каждую квартиру и не падали LazyInitializationException
 * при сериализации в JSON вне транзакции.
 */
@Repository
public interface ApartmentRepository extends JpaRepository<Apartment, Long> {

    /** Найти квартиру по avito_id, вместе с фото и бейджами */
    @EntityGraph(attributePaths = {"images", "badges"})
    Optional<Apartment> findByAvitoId(String avitoId);

    /** Проверить существование по avito_id */
    boolean existsByAvitoId(String avitoId);

    /** Все квартиры, вместе с фото и бейджами */
    @Override
    @EntityGraph(attributePaths = {"images", "badges"})
    List<Apartment> findAll();
}