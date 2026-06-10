package my.help.useful.finance.repository;

import my.help.useful.finance.entity.Account;
import my.help.useful.finance.entity.Cashback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CashbackRepository extends JpaRepository<Cashback, Long> {

    List<Cashback> findByAccount(Account account);

    List<Cashback> findByAccountId(Long accountId);

    List<Cashback> findByActiveTrue();

    @Query("SELECT c FROM Cashback c WHERE c.active = true AND " +
            "(c.validFrom IS NULL OR c.validFrom <= :date) AND " +
            "(c.validTo IS NULL OR c.validTo >= :date)")
    List<Cashback> findActiveCashbacksForDate(@Param("date") LocalDate date);

    @Query("SELECT c.account.bankName, MAX(c.percentage) FROM Cashback c WHERE c.active = true GROUP BY c.account.bankName")
    List<Object[]> findMaxCashbackByBank();

    Optional<Cashback> findByAccountBankNameAndCategory(String bankName, String category);
}