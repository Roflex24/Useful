package my.help.finance.general.repository;

import my.help.finance.general.entity.Security;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface SecurityRepository extends JpaRepository<Security, Long> {

    List<Security> findByAccountId(Long accountId);

    @Query("SELECT COALESCE(SUM(s.quantity * s.currentPrice), 0) FROM Security s WHERE s.account.id = :accountId")
    BigDecimal getTotalValueByAccountId(Long accountId);
}