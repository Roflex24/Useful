package my.help.finance.invest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

    private static final int FACE_VALUE = 1000;
    private static final double YTM_TOLERANCE = 1e-7;
    private static final int MAX_ITERATIONS = 1000;
    private static final int DEFAULT_COUPON_FREQUENCY = 2;

    private static final Set<String> MARKETDATA_FIELDS = Set.of(
            "LAST", "LCURRENTPRICE", "CLOSEPRICE", "MARKETPRICE", "WAPRICE",
            "ACCRUEDINT", "YIELD", "CLOSEYIELD", "YIELDATWAPRICE"
    );

    private static final Set<String> YIELDS_FIELDS = Set.of(
            "EFFECTIVEYIELD", "EFFECTIVEYIELDWAPRICE", "YIELDTOOFFER"
    );

    private static final Set<String> SECURITIES_FIELDS = Set.of(
            "SECID", "SHORTNAME", "ISIN", "MATDATE", "COUPONVALUE", "COUPONPERCENT",
            "COUPONPERIOD", "ACCRUEDINT", "PREVPRICE", "PREVWAPRICE", "FACEVALUE",
            "BONDTYPE", "BONDSUBTYPE"
    );

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final BondDailyDataRepository bondDailyDataRepository;
    private final BondMapper bondMapper;
    private final MoexApiClient moexApiClient;

    @Autowired
    public MoexService(BondDailyDataRepository bondDailyDataRepository,
                       BondMapper bondMapper,
                       MoexApiClient moexApiClient) {
        this.bondDailyDataRepository = bondDailyDataRepository;
        this.bondMapper = bondMapper;
        this.moexApiClient = moexApiClient;
    }

    public List<OFZBondSummary> fetchOFZDataWithStats() throws IOException {
        LocalDate today = LocalDate.now();

        // 1. Проверяем наличие данных за сегодня в БД
        List<BondDailyData> existingData = bondDailyDataRepository.findByDate(today);
        if (!existingData.isEmpty()) {
            logger.info("Returning {} bond records from database for date {}", existingData.size(), today);
            return existingData.stream()
                    .map(bondMapper::entityToSummary)
                    .collect(Collectors.toList());
        }

        // 2. Если данных нет — получаем с MOEX через API-клиент
        logger.info("No data in database for {}, fetching from MOEX", today);
        String jsonResponse = moexApiClient.fetchRawBondsJson();
        if (jsonResponse == null) {
            logger.error("Empty response entity from MOEX ISS");
            return Collections.emptyList();
        }

        // 3. Парсим и обогащаем данные
        List<OFZBond> bonds = parseAndEnrichBonds(jsonResponse);

        // 4. Сохраняем полученные записи в БД
        List<BondDailyData> entitiesToSave = bonds.stream()
                .map(bond -> bondMapper.toEntity(bond, today))
                .collect(Collectors.toList());
        bondDailyDataRepository.saveAll(entitiesToSave);
        logger.info("Saved {} bond records to database for date {}", entitiesToSave.size(), today);

        // 5. Возвращаем результат
        return bonds.stream()
                .map(bondMapper::toSummary)
                .collect(Collectors.toList());
    }

    private List<OFZBond> parseAndEnrichBonds(String json) throws IOException {
        JsonNode root = objectMapper.readTree(json);

        Map<String, Map<String, Object>> marketDataMap = parseMarketData(
                root.path("marketdata").path("data"),
                root.path("marketdata").path("columns"),
                MARKETDATA_FIELDS
        );

        Map<String, Map<String, Object>> yieldsMap = parseMarketData(
                root.path("marketdata_yields").path("data"),
                root.path("marketdata_yields").path("columns"),
                YIELDS_FIELDS
        );

        JsonNode securitiesData = root.path("securities").path("data");
        JsonNode securitiesColumns = root.path("securities").path("columns");
        Map<String, Integer> securitiesColumnIndexes = buildColumnIndexMap(securitiesColumns, SECURITIES_FIELDS);
        if (securitiesData.isArray() && !securitiesColumnIndexes.isEmpty()) {
            return processSecuritiesRows(securitiesData, securitiesColumnIndexes, marketDataMap, yieldsMap);
        } else {
            logger.warn("No securities data or missing required columns");
            return Collections.emptyList();
        }
    }

    private List<OFZBond> processSecuritiesRows(
            JsonNode securitiesData,
            Map<String, Integer> colIndexes,
            Map<String, Map<String, Object>> marketDataMap,
            Map<String, Map<String, Object>> yieldsMap) {

        List<OFZBond> bonds = new ArrayList<>();
        for (JsonNode row : securitiesData) {
            OFZBond bond = parseSecuritiesRow(row, colIndexes);
            if (bond.getIsin() == null || !bond.getIsin().startsWith("RU")) {
                continue;
            }

            if (bond.getBondType() == null) {
                bond.setBondType(determineBondTypeByCoupon(bond.getCouponPercent(), bond.getCouponValue(), bond));
            }

            String secid = bond.getSecid();
            Map<String, Object> market = marketDataMap.get(secid);
            Map<String, Object> yields = yieldsMap.get(secid);

            Double pricePercent = bond.getPricePercent();
            if (isInvalidPrice(pricePercent)) {
                pricePercent = extractPriceFromMarketData(market);
                bond.setPricePercent(pricePercent);
            }

            Double accruedInterest = bond.getAccruedInterest();
            if ((accruedInterest == null || accruedInterest <= 0) && market != null) {
                Double ai = getDouble(market, "ACCRUEDINT");
                if (ai != null && ai > 0) {
                    bond.setAccruedInterest(ai);
                    accruedInterest = ai;
                }
            }

            if (bond.getCouponFrequency() == null) {
                bond.setCouponFrequency(DEFAULT_COUPON_FREQUENCY);
            }

            if (isInvalidPrice(pricePercent)) {
                logger.warn("No price for bond: secid={}, isin={}", secid, bond.getIsin());
                bond.setPrice(0.0);
                bond.setYield(0.0);
                bonds.add(bond);
                continue;
            }

            bond.setPrice(pricePercent * 10.0);

            Double ytm = extractYield(yields, market);
            if ((ytm == null || ytm <= 0) && canCalculateYtm(bond)) {
                ytm = calculateYTMWithBisection(
                        pricePercent, bond.getCouponValue(), bond.getCouponFrequency(),
                        bond.getFaceValue(), accruedInterest != null ? accruedInterest : 0.0,
                        bond.getMaturityDate(), LocalDate.now()
                );
            }
            bond.setYield(ytm != null && ytm > 0 ? ytm : 0.0);
            bonds.add(bond);
        }
        logger.info("Total bonds parsed: {}", bonds.size());
        return bonds;
    }

    private OFZBond parseSecuritiesRow(JsonNode row, Map<String, Integer> colIndexes) {
        OFZBond bond = new OFZBond();
        Double pricePercent = null;

        for (Map.Entry<String, Integer> entry : colIndexes.entrySet()) {
            String column = entry.getKey();
            int index = entry.getValue();
            JsonNode value = row.get(index);
            if (value == null || value.isNull()) continue;

            switch (column) {
                case "SECID" -> bond.setSecid(value.asText());
                case "SHORTNAME" -> bond.setShortname(value.asText());
                case "ISIN" -> bond.setIsin(value.asText());
                case "MATDATE" -> bond.setMaturityDate(value.asText());
                case "COUPONVALUE" -> bond.setCouponValue(value.asDouble());
                case "COUPONPERCENT" -> bond.setCouponPercent(value.asDouble());
                case "COUPONPERIOD" -> {
                    int periodDays = value.asInt();
                    if (periodDays > 0) {
                        bond.setCouponFrequency(365 / periodDays);
                    }
                }
                case "ACCRUEDINT" -> bond.setAccruedInterest(value.asDouble());
                case "PREVPRICE" -> {
                    pricePercent = value.asDouble();
                    bond.setPricePercent(pricePercent);
                }
                case "PREVWAPRICE" -> {
                    if (isInvalidPrice(pricePercent) && value.asDouble() > 0) {
                        pricePercent = value.asDouble();
                        bond.setPricePercent(pricePercent);
                    }
                }
                case "FACEVALUE" -> bond.setFaceValue(value.asInt());
                case "BONDTYPE" -> bond.setBondType(value.asText());
                case "BONDSUBTYPE" -> bond.setBondSubType(value.asText());
            }
        }
        if (bond.getFaceValue() == null) bond.setFaceValue(FACE_VALUE);
        return bond;
    }

    private boolean isInvalidPrice(Double price) {
        return price == null || price.isNaN() || price <= 0;
    }

    private Double extractPriceFromMarketData(Map<String, Object> market) {
        if (market == null) return null;
        Double[] candidates = {
                getDouble(market, "LAST"),
                getDouble(market, "LCURRENTPRICE"),
                getDouble(market, "MARKETPRICE"),
                getDouble(market, "WAPRICE"),
                getDouble(market, "CLOSEPRICE")
        };
        for (Double candidate : candidates) {
            if (candidate != null && candidate > 0) return candidate;
        }
        return null;
    }

    private Double extractYield(Map<String, Object> yields, Map<String, Object> market) {
        if (yields != null) {
            Double[] yieldCandidates = {
                    getDouble(yields, "EFFECTIVEYIELD"),
                    getDouble(yields, "EFFECTIVEYIELDWAPRICE"),
                    getDouble(yields, "YIELDTOOFFER")
            };
            for (Double y : yieldCandidates) {
                if (y != null && y > 0) return y;
            }
        }
        if (market != null) {
            Double[] marketYieldCandidates = {
                    getDouble(market, "YIELD"),
                    getDouble(market, "CLOSEYIELD"),
                    getDouble(market, "YIELDATWAPRICE")
            };
            for (Double y : marketYieldCandidates) {
                if (y != null && y > 0) return y;
            }
        }
        return null;
    }

    private boolean canCalculateYtm(OFZBond bond) {
        String type = bond.getBondType();
        if (type == null) return false;
        String lowerType = type.toLowerCase();
        boolean isFloating = lowerType.contains("флоатер") ||
                lowerType.contains("перемен") ||
                lowerType.contains("floating");
        return !isFloating &&
                bond.getCouponValue() != null && bond.getCouponValue() > 0 &&
                bond.getMaturityDate() != null && !bond.getMaturityDate().isEmpty();
    }

    private Map<String, Map<String, Object>> parseMarketData(JsonNode data, JsonNode columns, Set<String> requiredFields) {
        Map<String, Map<String, Object>> result = new HashMap<>();
        if (!data.isArray() || !columns.isArray()) return result;

        Map<String, Integer> columnIndexes = buildColumnIndexMap(columns, requiredFields);
        if (columnIndexes.isEmpty()) return result;

        for (JsonNode row : data) {
            String secid = row.get(0).asText();
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

    private Map<String, Integer> buildColumnIndexMap(JsonNode columns, Set<String> fields) {
        Map<String, Integer> map = new HashMap<>();
        if (columns == null || !columns.isArray()) return map;
        for (int i = 0; i < columns.size(); i++) {
            String col = columns.get(i).asText();
            if (fields.contains(col)) {
                map.put(col, i);
            }
        }
        return map;
    }

    private String determineBondTypeByCoupon(Double couponPercent, Double couponValue, OFZBond bond) {
        if ((couponPercent == null || couponPercent == 0.0) &&
                (couponValue == null || couponValue == 0.0)) {
            return "Дисконтные (бескупонные)";
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
        if (value instanceof Double d) return d;
        if (value instanceof Integer i) return i.doubleValue();
        if (value instanceof Long l) return l.doubleValue();
        if (value instanceof String s) {
            try {
                return Double.parseDouble(s);
            } catch (NumberFormatException e) {
                logger.debug("Cannot parse double from string: {}", s);
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