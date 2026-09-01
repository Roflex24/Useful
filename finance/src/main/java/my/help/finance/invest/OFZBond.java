package my.help.finance.invest;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Setter;

public class OFZBond {
    @Setter
    private String secid;
    @Setter
    private String shortname;
    @Setter
    private String isin;
    @Setter
    private String maturityDate;
    @Setter
    private Double couponPercent;
    @Setter
    private Double couponValue;
    @Setter
    private Integer couponFrequency;
    @Setter
    private Double yield;
    @Setter
    private Double price;
    @Setter
    private Double pricePercent;
    @Setter
    private Double accruedInterest;
    @Setter
    private Integer faceValue;
    private String bondType;
    @Setter
    private String bondSubType;
    @Setter
    private String bondTypeDisplay;

    public OFZBond() {}

    @JsonProperty("secid")
    public String getSecid() { return secid; }

    @JsonProperty("shortname")
    public String getShortname() { return shortname; }

    @JsonProperty("isin")
    public String getIsin() { return isin; }

    @JsonProperty("maturityDate")
    public String getMaturityDate() { return maturityDate; }

    @JsonProperty("couponPercent")
    public Double getCouponPercent() { return couponPercent; }

    @JsonProperty("couponValue")
    public Double getCouponValue() { return couponValue; }

    @JsonProperty("couponFrequency")
    public Integer getCouponFrequency() { return couponFrequency; }

    @JsonProperty("yield")
    public Double getYield() { return yield; }

    @JsonProperty("price")
    public Double getPrice() { return price; }

    @JsonProperty("pricePercent")
    public Double getPricePercent() { return pricePercent; }

    @JsonProperty("accruedInterest")
    public Double getAccruedInterest() { return accruedInterest; }

    @JsonProperty("faceValue")
    public Integer getFaceValue() { return faceValue; }

    @JsonProperty("bondType")
    public String getBondType() { return bondType; }
    public void setBondType(String bondType) {
        this.bondType = bondType;
        this.bondTypeDisplay = determineBondTypeDisplay(bondType, couponPercent);
    }

    @JsonProperty("bondSubType")
    public String getBondSubType() { return bondSubType; }

    @JsonProperty("bondTypeDisplay")
    public String getBondTypeDisplay() { return bondTypeDisplay; }

    private String determineBondTypeDisplay(String bondType, Double couponPercent) {
        if (bondType == null) {
            return "Неизвестно";
        }

        return switch (bondType.toLowerCase()) {
            case "фикс с известным купоном", "фикс с известным купоном (до погашения)" -> "С фиксированным купоном";
            case "флоатер" -> "С переменным (плавающим) купоном";
            case "облигации с индексируемым номиналом", "линкер/облигации с индексируемым" -> "Индексируемые (линкеры)";
            case "амортизируемые облигации" -> "Амортизируемые";
            case "валютные облигации" -> "Валютные";
            default -> {
                if (couponPercent != null && couponPercent == 0) {
                    yield "Дисконтные (бескупонные)";
                }
                yield bondType;
            }
        };
    }
}