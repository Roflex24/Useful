package my.help.finance.invest.bond;

import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.List;

public class BondSpecification {

    public static Specification<BondDailyData> equalsDate(LocalDate date) {
        return (root, query, cb) -> {
            if(date == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("date"), date);
        };
    }

    public static Specification<BondDailyData> hasBondTypeDisplayIn(List<String> types) {
        return (root, query, cb) -> {
            if (types == null || types.isEmpty()) {
                return cb.conjunction();
            }
            return root.get("bondTypeDisplay").in(types);
        };
    }
}
