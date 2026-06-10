package my.help.useful.finance.repository;

import my.help.useful.finance.entity.Account;
import my.help.useful.finance.entity.Cashback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CashbackRepository extends JpaRepository<Cashback, Long> {

    List<Cashback> findByAccount(Account account);

    List<Cashback> findByAccountId(Long accountId);
}