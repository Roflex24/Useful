package my.help.finance.repository;

import my.help.finance.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    @Query("SELECT a.bankName, SUM(a.amount) FROM Account a GROUP BY a.bankName")
    List<Object[]> getSumByBank();

    @Query("SELECT a.type, SUM(a.amount) FROM Account a GROUP BY a.type")
    List<Object[]> getSumByType();

    @Query("SELECT COALESCE(SUM(a.amount), 0) FROM Account a")
    BigDecimal getTotalAmount();
}