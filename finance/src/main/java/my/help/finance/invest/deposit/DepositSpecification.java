package my.help.finance.invest.deposit;

import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.List;

public class DepositSpecification {

    public static Specification<DepositRateEntity> equalsDate(LocalDate date) {
        return (root, query, cb) -> {
            if (date == null) return cb.conjunction();
            return cb.equal(root.get("parseDate"), date);
        };
    }

    public static Specification<DepositRateEntity> hasPercentCalculationIn(List<String> types) {
        return (root, query, cb) -> {
            if (types == null || types.isEmpty()) return cb.conjunction();
            return root.get("percentCalculation").in(types);
        };
    }
}
