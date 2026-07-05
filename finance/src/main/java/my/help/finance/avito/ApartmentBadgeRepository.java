package my.help.finance.avito;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApartmentBadgeRepository extends JpaRepository<ApartmentBadge, Long> {
}
