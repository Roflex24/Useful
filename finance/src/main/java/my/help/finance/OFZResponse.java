package my.help.finance;

import java.util.List;
import java.util.Map;

public class OFZResponse {
    private List<OFZBond> bonds;
    private int totalCount;
    private double averageYield;
    private double minYield;
    private double maxYield;
    private double averagePrice;
    private Map<String, Long> bondTypeStats;  // Статистика по типам

    public OFZResponse() {}

    public OFZResponse(List<OFZBond> bonds, int totalCount, double averageYield,
                       double minYield, double maxYield, double averagePrice) {
        this.bonds = bonds;
        this.totalCount = totalCount;
        this.averageYield = averageYield;
        this.minYield = minYield;
        this.maxYield = maxYield;
        this.averagePrice = averagePrice;
    }

    // Геттеры и сеттеры
    public List<OFZBond> getBonds() { return bonds; }
    public void setBonds(List<OFZBond> bonds) { this.bonds = bonds; }

    public int getTotalCount() { return totalCount; }
    public void setTotalCount(int totalCount) { this.totalCount = totalCount; }

    public double getAverageYield() { return averageYield; }
    public void setAverageYield(double averageYield) { this.averageYield = averageYield; }

    public double getMinYield() { return minYield; }
    public void setMinYield(double minYield) { this.minYield = minYield; }

    public double getMaxYield() { return maxYield; }
    public void setMaxYield(double maxYield) { this.maxYield = maxYield; }

    public double getAveragePrice() { return averagePrice; }
    public void setAveragePrice(double averagePrice) { this.averagePrice = averagePrice; }

    public Map<String, Long> getBondTypeStats() { return bondTypeStats; }
    public void setBondTypeStats(Map<String, Long> bondTypeStats) { this.bondTypeStats = bondTypeStats; }
}