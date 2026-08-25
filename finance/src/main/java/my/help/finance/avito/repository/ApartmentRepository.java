package my.help.finance.avito.repository;

import my.help.finance.avito.entity.Apartment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для работы с квартирами.
 * Spring Data JPA автоматически реализует все стандартные методы:
 * save(), findAll(), findById() и т.д.
 * <p>
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

    /** Все квартиры, вместе с фото и бейджами */
    @Override
    @EntityGraph(attributePaths = {"images", "badges"})
    List<Apartment> findAll();

    /**
     * Очередь для бота-обходчика: объявления с известной ссылкой,
     * страницу которых ещё не скачали (detail_visited не true).
     * Сортировка по id — бот идёт по объявлениям в порядке загрузки,
     * это стабильно и предсказуемо между запусками.
     *
     * @EntityGraph здесь обязателен: бот работает в фоновом потоке без
     * HTTP-запроса (никакого Open-Session-In-View), поэтому Hibernate-сессия
     * закрывается сразу же после этого запроса. Пока бот проходит между
     * объявлениями (секунды-минуты паузы), сущности уже detached — если
     * images не подгружены заранее, попытка их прочитать/заменить в
     * AvitoDetailPageParserService (parsePhotos -> replaceImages) упадёт с
     * LazyInitializationException "no session".
     */
    @EntityGraph(attributePaths = {"images", "badges"})
    @Query("""
    select a from Apartment a
    where a.url is not null and (a.detailVisited is null or a.detailVisited = false)
    order by a.id asc
    """)
    List<Apartment> findQueueForBot();
}