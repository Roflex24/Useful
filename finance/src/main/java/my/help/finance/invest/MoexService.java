package my.help.finance.invest;

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
import java.util.stream.Collectors;

@Service
public class MoexService {

    private static final Logger logger = LoggerFactory.getLogger(MoexService.class);

    private static final String MOEX_ISS_URL = "https://iss.moex.com/iss/engines/stock/markets/bonds/boards/TQOB/securities.json";
    private static final int FACE_VALUE = 1000;
    private static final double YTM_TOLERANCE = 1e-7;
    private static final int MAX_ITERATIONS = 1000;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Списки полей, которые реально нужны
    private static final Set<String> MARKETDATA_FIELDS = Set.of(
            "LAST", "LCURRENTPRICE", "CLOSEPRICE", "MARKETPRICE", "WAPRICE",
            "ACCRUEDINT", "YIELD", "CLOSEYIELD", "YIELDATWAPRICE"
    );

    private static final Set<String> YIELDS_FIELDS = Set.of(
            "EFFECTIVEYIELD", "EFFECTIVEYIELDWAPRICE", "YIELDTOOFFER"
    );

    public List<OFZBondSummary> fetchOFZDataWithStats() throws IOException, ParseException {
        List<OFZBond> bonds = new ArrayList<>();

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(MOEX_ISS_URL);
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                String jsonResponse = EntityUtils.toString(response.getEntity());
                bonds = parseAndEnrichBonds(jsonResponse);
            }
        }

        return bonds.stream()
                .map(this::toSummary)
                .collect(Collectors.toList());
    }

    private OFZBondSummary toSummary(OFZBond bond) {
        OFZBondSummary summary = new OFZBondSummary();
        summary.setShortname(bond.getShortname());
        summary.setFaceValue(bond.getFaceValue());
        summary.setCouponValue(bond.getCouponValue());
        summary.setBondTypeDisplay(bond.getBondTypeDisplay());
        summary.setMaturityDate(bond.getMaturityDate());
        summary.setPrice(bond.getPrice());
        summary.setYield(bond.getYield());
        return summary;
    }

    private List<OFZBond> parseAndEnrichBonds(String json) throws IOException {
        List<OFZBond> bonds = new ArrayList<>();
        JsonNode root = objectMapper.readTree(json);

        // Парсим securities
        JsonNode securitiesData = root.path("securities").path("data");
        JsonNode securitiesColumns = root.path("securities").path("columns");

        // Парсим marketdata – только нужные поля
        Map<String, Map<String, Object>> marketDataMap = parseMarketData(
                root.path("marketdata").path("data"),
                root.path("marketdata").path("columns"),
                MARKETDATA_FIELDS
        );

        // Парсим marketdata_yields – только нужные поля
        Map<String, Map<String, Object>> yieldsMap = parseMarketData(
                root.path("marketdata_yields").path("data"),
                root.path("marketdata_yields").path("columns"),
                YIELDS_FIELDS
        );

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

                for (int i = 0; i < securitiesColumns.size(); i++) {
                    String columnName = securitiesColumns.get(i).asText();
                    JsonNode value = row.get(i);

                    if (value == null || value.isNull()) continue;

                    switch (columnName) {
                        case "SECID" -> {
                            secid = value.asText();
                            bond.setSecid(secid);
                        }
                        case "SHORTNAME" -> bond.setShortname(value.asText());
                        case "ISIN" -> bond.setIsin(value.asText());
                        case "MATDATE" -> {
                            maturityDateStr = value.asText();
                            bond.setMaturityDate(maturityDateStr);
                        }
                        case "COUPONVALUE" -> {
                            couponValue = value.asDouble();
                            bond.setCouponValue(couponValue);
                        }
                        case "COUPONPERCENT" -> {
                            couponPercent = value.asDouble();
                            bond.setCouponPercent(couponPercent);
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
                            bond.setAccruedInterest(accruedInterest);
                        }
                        case "PREVPRICE" -> {
                            pricePercent = value.asDouble();
                            bond.setPricePercent(pricePercent);
                        }
                        case "PREVWAPRICE" -> {
                            if (pricePercent == null && value.asDouble() > 0) {
                                pricePercent = value.asDouble();
                                bond.setPricePercent(pricePercent);
                            }
                        }
                        case "FACEVALUE" -> {
                            faceValue = value.asDouble();
                            bond.setFaceValue(value.asInt());
                        }
                        case "BONDTYPE" -> {
                            bondType = value.asText();
                            bond.setBondType(bondType);
                        }
                        case "BONDSUBTYPE" -> bond.setBondSubType(value.asText());
                    }
                }

                // Фильтр по ISIN
                if (bond.getIsin() == null || !bond.getIsin().startsWith("RU")) {
                    continue;
                }

                // Определяем тип, если не задан
                if (bondType == null) {
                    bondType = determineBondTypeByCoupon(couponPercent, couponValue, bond);
                    bond.setBondType(bondType);
                }

                Map<String, Object> market = marketDataMap.get(secid);
                Map<String, Object> yields = yieldsMap.get(secid);

                // Цена
                if (pricePercent == null || Double.isNaN(pricePercent) || pricePercent <= 0) {
                    if (market != null) {
                        Double last = getDouble(market, "LAST");
                        Double current = getDouble(market, "LCURRENTPRICE");
                        Double close = getDouble(market, "CLOSEPRICE");
                        Double mktPrice = getDouble(market, "MARKETPRICE");
                        Double waprice = getDouble(market, "WAPRICE");
                        if (last != null && last > 0) pricePercent = last;
                        else if (current != null && current > 0) pricePercent = current;
                        else if (mktPrice != null && mktPrice > 0) pricePercent = mktPrice;
                        else if (waprice != null && waprice > 0) pricePercent = waprice;
                        else if (close != null && close > 0) pricePercent = close;
                    }
                }

                // НКД
                if ((accruedInterest == null || accruedInterest <= 0) && market != null) {
                    Double ai = getDouble(market, "ACCRUEDINT");
                    if (ai != null && ai > 0) {
                        accruedInterest = ai;
                        bond.setAccruedInterest(ai);
                    }
                }

                // Частота купона по умолчанию
                if (couponFrequency == null) {
                    couponFrequency = 2;
                    bond.setCouponFrequency(2);
                }

                // Если цены нет – пропускаем
                if (pricePercent == null || Double.isNaN(pricePercent) || pricePercent <= 0) {
                    logger.warn("No price for bond: secid={}, isin={}", secid, bond.getIsin());
                    bond.setPrice(0.0);
                    bond.setYield(0.0);
                    bonds.add(bond);
                    continue;
                }

                bond.setPrice(pricePercent * 10.0);

                // Доходность
                Double ytm = null;

                // 1) из marketdata_yields
                if (yields != null) {
                    Double effYield = getDouble(yields, "EFFECTIVEYIELD");
                    Double effYieldWaprice = getDouble(yields, "EFFECTIVEYIELDWAPRICE");
                    Double yieldToOffer = getDouble(yields, "YIELDTOOFFER");
                    if (effYield != null && effYield > 0) ytm = effYield;
                    else if (effYieldWaprice != null && effYieldWaprice > 0) ytm = effYieldWaprice;
                    else if (yieldToOffer != null && yieldToOffer > 0) ytm = yieldToOffer;
                }

                // 2) из marketdata
                if ((ytm == null || ytm <= 0) && market != null) {
                    Double yield = getDouble(market, "YIELD");
                    Double closeYield = getDouble(market, "CLOSEYIELD");
                    Double yieldAtWaprice = getDouble(market, "YIELDATWAPRICE");
                    if (yield != null && yield > 0) ytm = yield;
                    else if (closeYield != null && closeYield > 0) ytm = closeYield;
                    else if (yieldAtWaprice != null && yieldAtWaprice > 0) ytm = yieldAtWaprice;
                }

                // 3) расчёт YTM
                if (ytm == null || ytm <= 0) {
                    if (bondType != null && !bondType.toLowerCase().contains("флоатер") &&
                            !bondType.toLowerCase().contains("перемен") &&
                            !bondType.toLowerCase().contains("floating")) {
                        if (couponValue != null && couponValue > 0 && maturityDateStr != null && !maturityDateStr.isEmpty()) {
                            try {
                                ytm = calculateYTMWithBisection(
                                        pricePercent, couponValue, couponFrequency,
                                        faceValue, accruedInterest != null ? accruedInterest : 0.0,
                                        maturityDateStr, LocalDate.now()
                                );
                            } catch (Exception e) {
                                logger.warn("Failed to calculate YTM for {}: {}", secid, e.getMessage());
                            }
                        }
                    } else {
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
     * Универсальный парсер для marketdata и marketdata_yields.
     * Извлекает только нужные поля (переданные в requiredFields) и складывает в Map по SECID.
     */
    private Map<String, Map<String, Object>> parseMarketData(JsonNode data, JsonNode columns, Set<String> requiredFields) {
        Map<String, Map<String, Object>> result = new HashMap<>();
        if (!data.isArray() || !columns.isArray()) return result;

        // Индексы нужных колонок
        Map<String, Integer> columnIndexes = new HashMap<>();
        for (int i = 0; i < columns.size(); i++) {
            String col = columns.get(i).asText();
            if (requiredFields.contains(col)) {
                columnIndexes.put(col, i);
            }
        }
        // Если нет ни одного нужного поля – выходим
        if (columnIndexes.isEmpty()) return result;

        for (JsonNode row : data) {
            String secid = row.get(0).asText(); // SECID всегда первая колонка
            Map<String, Object> rowMap = new HashMap<>();
            for (Map.Entry<String, Integer> entry : columnIndexes.entrySet()) {
                JsonNode value = row.get(entry.getValue());
                if (value != null && !value.isNull()) {
                    rowMap.put(entry.getKey(), convertValue(value));
                }
            }
            if (!rowMap.isEmpty()) {
                result.put(secid, rowMap);
            }
        }
        return result;
    }

    private String determineBondTypeByCoupon(Double couponPercent, Double couponValue, OFZBond bond) {
        if (couponPercent == null || couponPercent == 0) {
            if (couponValue == null || couponValue == 0) {
                return "Дисконтные (бескупонные)";
            }
        }
        if (bond.getBondSubType() != null && bond.getBondSubType().equals("Флоатер")) {
            return "С переменным (плавающим) купоном";
        }
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

    private Double getDouble(Map<String, Object> map, String key) {
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

    // ===== Методы расчёта YTM (без изменений) =====
    private double calculateYTMWithBisection(double pricePercent, double couponValue, int couponFrequency,
                                             double faceValue, double accruedInterest,
                                             String maturityDateStr, LocalDate settlementDate) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate maturityDate = LocalDate.parse(maturityDateStr, formatter);
            if (maturityDate.isBefore(settlementDate) || maturityDate.isEqual(settlementDate)) return 0.0;

            double dirtyPrice = (pricePercent / 100.0) * faceValue + accruedInterest;
            if (dirtyPrice <= 0) return 0.0;

            List<Payment> payments = buildPaymentSchedule(couponValue, couponFrequency, faceValue, maturityDate, settlementDate);
            if (payments.isEmpty()) return 0.0;

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
        if (couponFrequency <= 0) return payments;

        long daysInYear = 365;
        long periodDays = daysInYear / couponFrequency;
        if (periodDays <= 0) return payments;

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
            Payment lastPayment = payments.getLast();
            payments.set(payments.size() - 1, new Payment(lastPayment.amount + faceValue, lastPayment.date));
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
        if (priceAtLow < dirtyPrice) return 0.0;

        for (int iter = 0; iter < MAX_ITERATIONS; iter++) {
            double mid = (low + high) / 2.0;
            double priceAtMid = calculatePresentValue(payments, mid, settlementDate);
            double diff = priceAtMid - dirtyPrice;
            if (Math.abs(diff) < YTM_TOLERANCE) return mid * 100;
            if (diff > 0) low = mid;
            else high = mid;
        }
        return Math.max(0, ((low + high) / 2.0) * 100);
    }

    private double calculatePresentValue(List<Payment> payments, double ytm, LocalDate settlementDate) {
        double pv = 0.0;
        long daysInYear = 365;
        for (Payment payment : payments) {
            long daysUntil = ChronoUnit.DAYS.between(settlementDate, payment.date);
            if (daysUntil <= 0) continue;
            if (Math.abs(ytm) < 1e-10) {
                pv += payment.amount;
            } else {
                double yearsUntil = (double) daysUntil / daysInYear;
                pv += payment.amount / Math.pow(1 + ytm, yearsUntil);
            }
        }
        return pv;
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