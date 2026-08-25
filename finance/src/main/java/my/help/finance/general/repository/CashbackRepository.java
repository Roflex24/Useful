package my.help.finance.general.repository;

import my.help.finance.general.entity.Cashback;
import my.help.finance.general.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CashbackRepository extends JpaRepository<Cashback, Long> {

    List<Cashback> findByAccount(Account account);

    List<Cashback> findByAccountId(Long accountId);
}