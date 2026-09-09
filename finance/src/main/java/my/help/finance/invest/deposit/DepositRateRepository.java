package my.help.finance.invest.deposit;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface DepositRateRepository extends JpaRepository<DepositRateEntity, Long>, JpaSpecificationExecutor<DepositRateEntity> {
}