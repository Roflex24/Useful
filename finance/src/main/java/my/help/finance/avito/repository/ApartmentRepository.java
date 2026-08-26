package my.help.finance.avito.repository;

import my.help.finance.avito.entity.Apartment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApartmentRepository extends JpaRepository<Apartment, Long> {

    @EntityGraph(attributePaths = {"images", "badges"})
    Optional<Apartment> findByAvitoId(String avitoId);

    @Override
    @EntityGraph(attributePaths = {"images", "badges"})
    List<Apartment> findAll();

    @EntityGraph(attributePaths = {"images", "badges"})
    @Query("""
    select a from Apartment a
    where a.url is not null and (a.detailVisited is null or a.detailVisited = false)
    order by a.id asc
    """)
    List<Apartment> findQueueForBot();
}