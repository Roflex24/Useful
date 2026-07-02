package my.help.finance;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class MoexService {

    private static final Logger logger = LoggerFactory.getLogger(MoexService.class);

    private static final String MOEX_ISS_URL = "https://iss.moex.com/iss/engines/stock/markets/bonds/boards/TQOB/securities.json";
    private static final int FACE_VALUE = 1000;
    private static final double YTM_TOLERANCE = 1e-7;
    private static final int MAX_ITERATIONS = 1000;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Кэш для YTM
    private final Map<String, Double> ytmCache = new ConcurrentHashMap<>();

    public OFZResponse fetchOFZDataWithStats() throws IOException, ParseException {
        List<OFZBond> bonds = new ArrayList<>();

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(MOEX_ISS_URL);

            try (CloseableHttpResponse response = httpClient.execute(request)) {
                String jsonResponse = EntityUtils.toString(response.getEntity());
                bonds = parseAndEnrichBonds(jsonResponse);
            }
        }

        return calculateStatistics(bonds);
    }

    private List<OFZBond> parseAndEnrichBonds(String json) throws IOException {
        List<OFZBond> bonds = new ArrayList<>();
        JsonNode root = objectMapper.readTree(json);

        // Парсим securities данные
        JsonNode securitiesNode = root.path("securities");
        JsonNode securitiesData = securitiesNode.path("data");
        JsonNode securitiesColumns = securitiesNode.path("columns");

        // Парсим marketdata данные (актуальные цены и доходности)
        JsonNode marketDataNode = root.path("marketdata");
        JsonNode marketData = marketDataNode.path("data");
        JsonNode marketDataColumns = marketDataNode.path("columns");

        // Парсим marketdata_yields (официальные доходности)
        JsonNode yieldsNode = root.path("marketdata_yields");
        JsonNode yieldsData = yieldsNode.path("data");
        JsonNode yieldsColumns = yieldsNode.path("columns");

        // Создаем карты для быстрого доступа по SECID
        Map<String, Map<String, Object>> marketDataMap = new HashMap<>();
        Map<String, Map<String, Object>> yieldsMap = new HashMap<>();

        // Заполняем marketdata
        if (marketData.isArray() && marketDataColumns.isArray()) {
            for (JsonNode row : marketData) {
                Map<String, Object> rowData = new HashMap<>();
                String secid = null;
                for (int i = 0; i < marketDataColumns.size(); i++) {
                    String columnName = marketDataColumns.get(i).asText();
                    JsonNode value = row.get(i);
                    if (value != null && !value.isNull()) {
                        if ("SECID".equals(columnName)) {
                            secid = value.asText();
                        }
                        rowData.put(columnName, convertValue(value));
                    }
                }
                if (secid != null) {
                    marketDataMap.put(secid, rowData);
                }
            }
        }

        // Заполняем yields
        if (yieldsData.isArray() && yieldsColumns.isArray()) {
            for (JsonNode row : yieldsData) {
                Map<String, Object> rowData = new HashMap<>();
                String secid = null;
                for (int i = 0; i < yieldsColumns.size(); i++) {
                    String columnName = yieldsColumns.get(i).asText();
                    JsonNode value = row.get(i);
                    if (value != null && !value.isNull()) {
                        if ("SECID".equals(columnName)) {
                            secid = value.asText();
                        }
                        rowData.put(columnName, convertValue(value));
                    }
                }
                if (secid != null) {
                    yieldsMap.put(secid, rowData);
                }
            }
        }

        // Парсим securities
        if (securitiesData.isArray() && securitiesColumns.isArray()) {
            for (JsonNode row : securitiesData) {
                OFZBond bond = new OFZBond();
                String secid = null;
                Double pricePercent = null;
                Double couponValue = null;
                Integer couponFrequency = null;
                Double couponPercent = null;
                Double accruedInterest = null;
                String maturityDateStr = null;
                Double faceValue = (double) FACE_VALUE;
                String bondType = null;
                String bondSubType = null;

                // Парсим securities данные
                for (int i = 0; i < securitiesColumns.size(); i++) {
                    String columnName = securitiesColumns.get(i).asText();
                    JsonNode value = row.get(i);

                    if (value == null || value.isNull()) {
                        continue;
                    }

                    switch (columnName) {
                        case "SECID" -> {
                            secid = value.asText();
                            bond.setSecid(value.asText());
                        }
                        case "SHORTNAME" -> bond.setShortname(value.asText());
                        case "ISIN" -> bond.setIsin(value.asText());
                        case "MATDATE" -> {
                            maturityDateStr = value.asText();
                            bond.setMaturityDate(value.asText());
                        }
                        case "COUPONVALUE" -> {
                            couponValue = value.asDouble();
                            bond.setCouponValue(value.asDouble());
                        }
                        case "COUPONPERCENT" -> {
                            couponPercent = value.asDouble();
                            bond.setCouponPercent(value.asDouble());
                        }
                        case "COUPONPERIOD" -> {
                            int periodDays = value.asInt();
                            if (periodDays > 0) {
                                couponFrequency = 365 / periodDays;
                                bond.setCouponFrequency(couponFrequency);
                            }
                        }
                        case "ACCRUEDINT" -> {
                            accruedInterest = value.asDouble();
                            bond.setAccruedInterest(value.asDouble());
                        }
                        case "PREVPRICE" -> {
                            if (value != null && !value.isNull()) {
                                pricePercent = value.asDouble();
                                bond.setPricePercent(value.asDouble());
                            }
                        }
                        case "PREVWAPRICE" -> {
                            if (pricePercent == null && value != null && !value.isNull() && value.asDouble() > 0) {
                                pricePercent = value.asDouble();
                                bond.setPricePercent(value.asDouble());
                            }
                        }
                        case "FACEVALUE" -> {
                            if (value != null && !value.isNull()) {
                                faceValue = value.asDouble();
                                bond.setFaceValue(value.asInt());
                            }
                        }
                        case "BONDTYPE" -> {
                            bondType = value.asText();
                            bond.setBondType(value.asText());
                        }
                        case "BONDSUBTYPE" -> {
                            bondSubType = value.asText();
                            bond.setBondSubType(value.asText());
                        }
                    }
                }

                // Фильтруем только ОФЗ
                if (bond.getIsin() == null || !bond.getIsin().startsWith("RU")) {
                    continue;
                }

                // Если нет типа облигации, определяем по купону
                if (bondType == null) {
                    bondType = determineBondTypeByCoupon(couponPercent, couponValue, bond);
                    bond.setBondType(bondType);
                }

                // Если нет цены из securities, берем из marketdata
                Map<String, Object> marketsData = marketDataMap.get(secid);
                if (pricePercent == null || Double.isNaN(pricePercent) || pricePercent <= 0) {
                    if (marketData != null) {
                        Double lastPrice = getDoubleFromMap(marketsData, "LAST");
                        Double currentPrice = getDoubleFromMap(marketsData, "LCURRENTPRICE");
                        Double closePrice = getDoubleFromMap(marketsData, "CLOSEPRICE");
                        Double marketPrice = getDoubleFromMap(marketsData, "MARKETPRICE");
                        Double waprice = getDoubleFromMap(marketsData, "WAPRICE");

                        if (lastPrice != null && lastPrice > 0) pricePercent = lastPrice;
                        else if (currentPrice != null && currentPrice > 0) pricePercent = currentPrice;
                        else if (marketPrice != null && marketPrice > 0) pricePercent = marketPrice;
                        else if (waprice != null && waprice > 0) pricePercent = waprice;
                        else if (closePrice != null && closePrice > 0) pricePercent = closePrice;
                    }
                }

                // Если нет НКД, берем из marketdata
                if (accruedInterest == null || accruedInterest <= 0) {
                    if (marketData != null) {
                        Double accrued = getDoubleFromMap(marketsData, "ACCRUEDINT");
                        if (accrued != null && accrued > 0) {
                            accruedInterest = accrued;
                            bond.setAccruedInterest(accrued);
                        }
                    }
                }

                // Если нет частоты купонов, ставим по умолчанию
                if (couponFrequency == null) {
                    couponFrequency = 2;
                    bond.setCouponFrequency(2);
                }

                // Пропускаем облигации без цены
                if (pricePercent == null || Double.isNaN(pricePercent) || pricePercent <= 0) {
                    logger.warn("No price for bond: secid={}, isin={}", secid, bond.getIsin());
                    bond.setPrice(0.0);
                    bond.setYield(0.0);
                    bonds.add(bond);
                    continue;
                }

                // Пересчитываем цену в рубли
                bond.setPrice(pricePercent * 10.0);

                // Получаем доходность
                Double ytm = null;

                // 1. Сначала пробуем из marketdata_yields
                Map<String, Object> yieldData = yieldsMap.get(secid);
                if (yieldData != null) {
                    Double effectiveYield = getDoubleFromMap(yieldData, "EFFECTIVEYIELD");
                    Double yieldAtWaprice = getDoubleFromMap(yieldData, "EFFECTIVEYIELDWAPRICE");
                    Double yieldToOffer = getDoubleFromMap(yieldData, "YIELDTOOFFER");

                    if (effectiveYield != null && effectiveYield > 0) ytm = effectiveYield;
                    else if (yieldAtWaprice != null && yieldAtWaprice > 0) ytm = yieldAtWaprice;
                    else if (yieldToOffer != null && yieldToOffer > 0) ytm = yieldToOffer;
                }

                // 2. Если нет, пробуем из marketdata
                if (ytm == null || ytm <= 0) {
                    if (marketData != null) {
                        Double yield = getDoubleFromMap(marketsData, "YIELD");
                        Double closeYield = getDoubleFromMap(marketsData, "CLOSEYIELD");
                        Double yieldAtWaprice = getDoubleFromMap(marketsData, "YIELDATWAPRICE");

                        if (yield != null && yield > 0) ytm = yield;
                        else if (closeYield != null && closeYield > 0) ytm = closeYield;
                        else if (yieldAtWaprice != null && yieldAtWaprice > 0) ytm = yieldAtWaprice;
                    }
                }

                // 3. Если все еще нет, считаем сами
                if (ytm == null || ytm <= 0) {
                    // Для флоатеров не считаем YTM
                    if (bondType != null && !bondType.toLowerCase().contains("флоатер") &&
                            !bondType.toLowerCase().contains("перемен") &&
                            !bondType.toLowerCase().contains("floating")) {

                        if (couponValue != null && couponValue > 0 && maturityDateStr != null && !maturityDateStr.isEmpty()) {
                            try {
                                ytm = calculateYTMWithBisection(
                                        pricePercent,
                                        couponValue,
                                        couponFrequency,
                                        faceValue,
                                        accruedInterest != null ? accruedInterest : 0.0,
                                        maturityDateStr,
                                        LocalDate.now()
                                );
                            } catch (Exception e) {
                                logger.warn("Failed to calculate YTM for {}: {}", secid, e.getMessage());
                            }
                        }
                    } else {
                        // Для флоатеров используем текущую ставку
                        logger.debug("Floating rate bond, skipping YTM calculation: {}", secid);
                    }
                }

                bond.setYield(ytm != null && ytm > 0 ? ytm : 0.0);
                bonds.add(bond);
            }
        }

        logger.info("Total bonds parsed: {}", bonds.size());
        return bonds;
    }

    /**
     * Определение типа облигации по купону
     */
    private String determineBondTypeByCoupon(Double couponPercent, Double couponValue, OFZBond bond) {
        // Проверяем на бескупонную (дисконтную)
        if (couponPercent == null || couponPercent == 0) {
            if (couponValue == null || couponValue == 0) {
                return "Дисконтные (бескупонные)";
            }
        }

        // Проверяем на флоатер (обычно указывается в поле BONDTYPE)
        // Но если его нет, то по косвенным признакам
        if (bond.getBondSubType() != null && bond.getBondSubType().equals("Флоатер")) {
            return "С переменным (плавающим) купоном";
        }

        // По умолчанию - фиксированный
        return "С фиксированным купоном";
    }

    private Object convertValue(JsonNode value) {
        if (value.isTextual()) return value.asText();
        if (value.isInt()) return value.asInt();
        if (value.isLong()) return value.asLong();
        if (value.isDouble()) return value.asDouble();
        if (value.isBoolean()) return value.asBoolean();
        return null;
    }

    private Double getDoubleFromMap(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return null;
        if (value instanceof Double) return (Double) value;
        if (value instanceof Integer) return ((Integer) value).doubleValue();
        if (value instanceof Long) return ((Long) value).doubleValue();
        if (value instanceof String) {
            try {
                return Double.parseDouble((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    private double calculateYTMWithBisection(double pricePercent, double couponValue, int couponFrequency,
                                             double faceValue, double accruedInterest,
                                             String maturityDateStr, LocalDate settlementDate) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate maturityDate = LocalDate.parse(maturityDateStr, formatter);

            if (maturityDate.isBefore(settlementDate) || maturityDate.isEqual(settlementDate)) {
                return 0.0;
            }

            double dirtyPrice = (pricePercent / 100.0) * faceValue + accruedInterest;
            if (dirtyPrice <= 0) {
                return 0.0;
            }

            List<Payment> payments = buildPaymentSchedule(
                    couponValue,
                    couponFrequency,
                    faceValue,
                    maturityDate,
                    settlementDate
            );

            if (payments.isEmpty()) {
                return 0.0;
            }

            return findYTMByBisection(payments, dirtyPrice, settlementDate);

        } catch (Exception e) {
            logger.error("Error calculating YTM: {}", e.getMessage());
            return 0.0;
        }
    }

    private List<Payment> buildPaymentSchedule(double couponValue, int couponFrequency,
                                               double faceValue, LocalDate maturityDate,
                                               LocalDate settlementDate) {
        List<Payment> payments = new ArrayList<>();

        if (couponFrequency <= 0) {
            return payments;
        }

        long daysInYear = 365;
        long periodDays = daysInYear / couponFrequency;

        if (periodDays <= 0) {
            return payments;
        }

        LocalDate nextCouponDate = settlementDate.plusDays(periodDays);
        int maxPayments = 100;
        int paymentCount = 0;

        while (nextCouponDate.isBefore(maturityDate) && paymentCount < maxPayments) {
            if (ChronoUnit.DAYS.between(settlementDate, nextCouponDate) > 0) {
                payments.add(new Payment(couponValue, nextCouponDate));
                paymentCount++;
            }
            nextCouponDate = nextCouponDate.plusDays(periodDays);
        }

        if (!payments.isEmpty()) {
            Payment lastPayment = payments.get(payments.size() - 1);
            payments.set(payments.size() - 1,
                    new Payment(lastPayment.amount + faceValue, lastPayment.date));
        } else {
            payments.add(new Payment(faceValue, maturityDate));
        }

        return payments;
    }

    private double findYTMByBisection(List<Payment> payments, double dirtyPrice, LocalDate settlementDate) {
        double low = 0.0;
        double high = 1.0;

        double priceAtLow = calculatePresentValue(payments, low, settlementDate);
        double priceAtHigh = calculatePresentValue(payments, high, settlementDate);

        while (priceAtHigh > dirtyPrice && high < 10.0) {
            high *= 2;
            priceAtHigh = calculatePresentValue(payments, high, settlementDate);
        }

        if (priceAtLow < dirtyPrice) {
            return 0.0;
        }

        for (int iter = 0; iter < MAX_ITERATIONS; iter++) {
            double mid = (low + high) / 2.0;
            double priceAtMid = calculatePresentValue(payments, mid, settlementDate);
            double difference = priceAtMid - dirtyPrice;

            if (Math.abs(difference) < YTM_TOLERANCE) {
                return mid * 100;
            }

            if (difference > 0) {
                low = mid;
            } else {
                high = mid;
            }
        }

        double ytm = ((low + high) / 2.0) * 100;
        return Math.max(0, ytm);
    }

    private double calculatePresentValue(List<Payment> payments, double ytm, LocalDate settlementDate) {
        double pv = 0.0;
        long daysInYear = 365;

        for (Payment payment : payments) {
            long daysUntil = ChronoUnit.DAYS.between(settlementDate, payment.date);
            if (daysUntil <= 0) {
                continue;
            }

            if (Math.abs(ytm) < 1e-10) {
                pv += payment.amount;
            } else {
                double yearsUntil = (double) daysUntil / daysInYear;
                double discountFactor = Math.pow(1 + ytm, yearsUntil);
                pv += payment.amount / discountFactor;
            }
        }
        return pv;
    }

    private OFZResponse calculateStatistics(List<OFZBond> bonds) {
        int totalCount = bonds.size();

        // Статистика по типам облигаций
        Map<String, Long> bondTypeStats = bonds.stream()
                .filter(b -> b.getBondTypeDisplay() != null)
                .collect(Collectors.groupingBy(
                        OFZBond::getBondTypeDisplay,
                        Collectors.counting()
                ));

        List<OFZBond> bondsWithYield = bonds.stream()
                .filter(b -> b.getYield() != null && !Double.isNaN(b.getYield()) && b.getYield() > 0)
                .toList();

        List<OFZBond> bondsWithPrice = bonds.stream()
                .filter(b -> b.getPrice() != null && !Double.isNaN(b.getPrice()) && b.getPrice() > 0)
                .toList();

        double averageYield = bondsWithYield.stream()
                .mapToDouble(OFZBond::getYield)
                .average()
                .orElse(0.0);

        double minYield = bondsWithYield.stream()
                .mapToDouble(OFZBond::getYield)
                .min()
                .orElse(0.0);

        double maxYield = bondsWithYield.stream()
                .mapToDouble(OFZBond::getYield)
                .max()
                .orElse(0.0);

        double averagePrice = bondsWithPrice.stream()
                .mapToDouble(OFZBond::getPrice)
                .average()
                .orElse(0.0);

        OFZResponse response = new OFZResponse(
                bonds,
                totalCount,
                averageYield,
                minYield,
                maxYield,
                averagePrice
        );

        // Добавляем статистику по типам
        response.setBondTypeStats(bondTypeStats);

        return response;
    }

    static class Payment {
        double amount;
        LocalDate date;

        Payment(double amount, LocalDate date) {
            this.amount = amount;
            this.date = date;
        }
    }
}