package my.help.finance;

import com.fasterxml.jackson.annotation.JsonProperty;

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
    private Double duration;
    private Integer faceValue;
    private String bondType;        // Тип облигации из MOEX
    private String bondSubType;     // Подтип облигации
    private String bondTypeDisplay; // Отображаемый тип (для API)

    // Конструкторы
    public OFZBond() {}

    // Геттеры и сеттеры
    @JsonProperty("secid")
    public String getSecid() { return secid; }
    public void setSecid(String secid) { this.secid = secid; }

    @JsonProperty("shortname")
    public String getShortname() { return shortname; }
    public void setShortname(String shortname) { this.shortname = shortname; }

    @JsonProperty("isin")
    public String getIsin() { return isin; }
    public void setIsin(String isin) { this.isin = isin; }

    @JsonProperty("maturityDate")
    public String getMaturityDate() { return maturityDate; }
    public void setMaturityDate(String maturityDate) { this.maturityDate = maturityDate; }

    @JsonProperty("couponPercent")
    public Double getCouponPercent() { return couponPercent; }
    public void setCouponPercent(Double couponPercent) { this.couponPercent = couponPercent; }

    @JsonProperty("couponValue")
    public Double getCouponValue() { return couponValue; }
    public void setCouponValue(Double couponValue) { this.couponValue = couponValue; }

    @JsonProperty("couponFrequency")
    public Integer getCouponFrequency() { return couponFrequency; }
    public void setCouponFrequency(Integer couponFrequency) { this.couponFrequency = couponFrequency; }

    @JsonProperty("yield")
    public Double getYield() { return yield; }
    public void setYield(Double yield) { this.yield = yield; }

    @JsonProperty("price")
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    @JsonProperty("pricePercent")
    public Double getPricePercent() { return pricePercent; }
    public void setPricePercent(Double pricePercent) { this.pricePercent = pricePercent; }

    @JsonProperty("accruedInterest")
    public Double getAccruedInterest() { return accruedInterest; }
    public void setAccruedInterest(Double accruedInterest) { this.accruedInterest = accruedInterest; }

    @JsonProperty("duration")
    public Double getDuration() { return duration; }
    public void setDuration(Double duration) { this.duration = duration; }

    @JsonProperty("faceValue")
    public Integer getFaceValue() { return faceValue; }
    public void setFaceValue(Integer faceValue) { this.faceValue = faceValue; }

    @JsonProperty("bondType")
    public String getBondType() { return bondType; }
    public void setBondType(String bondType) {
        this.bondType = bondType;
        this.bondTypeDisplay = determineBondTypeDisplay(bondType, couponPercent);
    }

    @JsonProperty("bondSubType")
    public String getBondSubType() { return bondSubType; }
    public void setBondSubType(String bondSubType) { this.bondSubType = bondSubType; }

    @JsonProperty("bondTypeDisplay")
    public String getBondTypeDisplay() { return bondTypeDisplay; }
    public void setBondTypeDisplay(String bondTypeDisplay) { this.bondTypeDisplay = bondTypeDisplay; }

    /**
     * Определение типа облигации на основе данных MOEX
     */
    private String determineBondTypeDisplay(String bondType, Double couponPercent) {
        if (bondType == null) {
            return "Неизвестно";
        }

        // Маппинг типов из MOEX
        switch (bondType.toLowerCase()) {
            case "фикс с известным купоном":
            case "фикс с известным купоном (до погашения)":
                return "С фиксированным купоном";
            case "флоатер":
                return "С переменным (плавающим) купоном";
            case "облигации с индексируемым номиналом":
            case "линкер/облигации с индексируемым":
                return "Индексируемые (линкеры)";
            case "амортизируемые облигации":
                return "Амортизируемые";
            case "валютные облигации":
                return "Валютные";
            default:
                // Дополнительная логика для бескупонных
                if (couponPercent != null && couponPercent == 0) {
                    return "Дисконтные (бескупонные)";
                }
                return bondType;
        }
    }
}