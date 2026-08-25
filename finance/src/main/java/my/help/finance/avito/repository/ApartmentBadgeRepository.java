package my.help.finance.avito.repository;

import my.help.finance.avito.entity.ApartmentBadge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApartmentBadgeRepository extends JpaRepository<ApartmentBadge, Long> {
}
