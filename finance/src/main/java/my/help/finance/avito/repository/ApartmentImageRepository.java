package my.help.finance.avito.repository;

import my.help.finance.avito.entity.ApartmentImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApartmentImageRepository extends JpaRepository<ApartmentImage, Long> {
}
