package my.help.finance.invest;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OFZBond {
    private String secid;
    private String shortname;
    private String isin;
    private String maturityDate;
    private Double couponPercent;
    private Double couponValue;
    private Integer couponFrequency;
    private Double yield;
    private Double price;
    private Double pricePercent;
    private Double accruedInterest;
    private Integer faceValue;
    private String bondType;
    private String bondSubType;

    @JsonProperty("bondTypeDisplay")
    public String getBondTypeDisplay() {
        return determineBondTypeDisplay(bondType, couponPercent);
    }

    private String determineBondTypeDisplay(String bondType, Double couponPercent) {
        if (bondType == null) {
            return "Неизвестно";
        }

        return switch (bondType.toLowerCase()) {
            case "фикс с известным купоном", "фикс с известным купоном (до погашения)" ->
                    "С фиксированным купоном";
            case "флоатер" ->
                    "С переменным (плавающим) купоном";
            case "облигации с индексируемым номиналом", "линкер/облигации с индексируемым" ->
                    "Индексируемые (линкеры)";
            case "амортизируемые облигации" ->
                    "Амортизируемые";
            case "валютные облигации" ->
                    "Валютные";
            default -> {
                if (couponPercent != null && couponPercent == 0) {
                    yield "Дисконтные (бескупонные)";
                }
                yield bondType;
            }
        };
    }
}