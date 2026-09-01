package my.help.finance.invest;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class OFZBondSummary {
    private String shortname;        // название
    private Integer faceValue;       // номинал
    private Double couponValue;      // купон (размер выплаты)
    private String bondTypeDisplay;  // тип купона (отображаемое значение)
    private String maturityDate;     // дата погашения
    private Double price;            // текущая цена (в рублях)
    private Double yield;            // доходность к погашению (YTM)

}