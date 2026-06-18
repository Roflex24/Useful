package my.help.finance.repository;

import my.help.finance.entity.Cashback;
import my.help.finance.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CashbackRepository extends JpaRepository<Cashback, Long> {

    List<Cashback> findByAccount(Account account);

    List<Cashback> findByAccountId(Long accountId);
}