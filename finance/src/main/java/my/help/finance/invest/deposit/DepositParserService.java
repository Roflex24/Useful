package my.help.finance.invest.deposit;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DepositParserService {

    private static final Logger logger = LoggerFactory.getLogger(DepositParserService.class);

    private static final String API_URL_BASE =
            "https://www.banki.ru/products/deposits/api/group/nizhniy_novgorod/" +
                    "?amount=0&currency=RUB&period=1y&special%5B%5D=14&top_hundred_place=0" +
                    "&partial_withdrawal=0&replenishment=0&payment_period_per_month=0" +
                    "&capitalization=0&early_termination_method=0&is_no_additional_expenses=0" +
                    "&is_only_bankiru_offer=0&type=14&pageMarketPlace=1&city=nizhniy_novgorod" +
                    "&per_page=10&exclude_special%5B%5D=8&is_fair_rate=1" +
                    "&segmentation_page=deposits_main&order=desc&sort=popular" +
                    "&page_type=MAINPRODUCT_SEARCH&isMobileApp=false" +
                    "&aff_sub2=/products/deposits/&is_main_page=1&page=";

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final DepositRateRepository depositRateRepository;

    public DepositParserService(DepositRateRepository depositRateRepository) {
        this.depositRateRepository = depositRateRepository;
    }

    /**
     * Возвращает данные о депозитах за сегодняшний день.
     * Если данные ещё не собраны, выполняет парсинг API и сохраняет в БД.
     */
    @Transactional()
    public DepositRatesResponse getDepositRatesForToday() {
        LocalDate today = LocalDate.now();
        logger.info("Getting deposit rates for date: {}", today);

        List<DepositRateEntity> entities = depositRateRepository.findByParseDate(today);
        if (!entities.isEmpty()) {
            logger.info("Found {} existing deposit rate entities for today, returning cached data.", entities.size());
            return buildResponseFromEntities(entities);
        }

        logger.info("No data found for today, starting parsing from API.");
        return parseAndSave(today);
    }

    /**
     * Выполняет парсинг API и сохраняет результаты в БД.
     */
    @Transactional
    protected DepositRatesResponse parseAndSave(LocalDate parseDate) {
        logger.info("Starting deposit rate parsing for date: {}", parseDate);
        List<DepositRateDto> rates = new ArrayList<>();

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        try {
            String firstPageUrl = API_URL_BASE + "1";
            logger.debug("Fetching first page: {}", firstPageUrl);
            HttpResponse<String> firstResponse = sendRequest(client, firstPageUrl);
            if (firstResponse.statusCode() != 200) {
                logger.error("HTTP error on first page, status: {}", firstResponse.statusCode());
                throw new RuntimeException("HTTP error: " + firstResponse.statusCode());
            }

            JsonNode firstRoot = objectMapper.readTree(firstResponse.body());
            int totalBanks = firstRoot.path("total").asInt();
            int perPage = firstRoot.path("per_page").asInt();
            int pages = (int) Math.ceil((double) totalBanks / perPage);
            logger.info("Total banks: {}, per page: {}, total pages: {}", totalBanks, perPage, pages);

            parsePage(firstRoot, rates);

            for (int page = 2; page <= pages; page++) {
                String url = API_URL_BASE + page;
                logger.debug("Fetching page {}/{}", page, pages);
                HttpResponse<String> response = sendRequest(client, url);
                if (response.statusCode() != 200) {
                    logger.error("HTTP error on page {}, status: {}", page, response.statusCode());
                    throw new RuntimeException("HTTP error: " + response.statusCode());
                }
                JsonNode root = objectMapper.readTree(response.body());
                parsePage(root, rates);
            }

            logger.info("Parsed {} deposit rate entries from API.", rates.size());
            saveAll(rates, parseDate);
            logger.info("Successfully saved {} entries to database for date {}", rates.size(), parseDate);

            return new DepositRatesResponse(totalBanks, rates);

        } catch (Exception e) {
            logger.error("Failed to fetch data from banki.ru for date {}", parseDate, e);
            throw new RuntimeException("Failed to fetch data from banki.ru", e);
        }
    }

    private void saveAll(List<DepositRateDto> rates, LocalDate parseDate) {
        List<DepositRateEntity> entities = rates.stream()
                .map(dto -> new DepositRateEntity(
                        null,
                        dto.bankName(),
                        dto.rateMin(),
                        dto.percentCalculation(),
                        parseDate))
                .collect(Collectors.toList());
        depositRateRepository.saveAll(entities);
        logger.debug("Saved {} entities to repository.", entities.size());
    }

    private DepositRatesResponse buildResponseFromEntities(List<DepositRateEntity> entities) {
        List<DepositRateDto> dtos = entities.stream()
                .map(e -> new DepositRateDto(e.getBankName(), e.getRateMin(), e.getPercentCalculation()))
                .collect(Collectors.toList());

        int totalBanks = (int) entities.stream()
                .map(DepositRateEntity::getBankName)
                .distinct()
                .count();

        logger.debug("Built response from {} entities, total banks: {}", entities.size(), totalBanks);
        return new DepositRatesResponse(totalBanks, dtos);
    }

    private HttpResponse<String> sendRequest(HttpClient client, String url) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(15))
                .header("Accept", "application/json")
                .header("User-Agent", "Mozilla/5.0")
                .GET()
                .build();
        logger.debug("Sending GET request to {}", url);
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private void parsePage(JsonNode root, List<DepositRateDto> result) {
        JsonNode groupedTable = root.path("grouped_table");
        if (!groupedTable.isArray()) {
            logger.warn("grouped_table is not an array or missing in response");
            return;
        }

        int parsedCount = 0;
        for (JsonNode bankGroup : groupedTable) {
            JsonNode rows = bankGroup.path("deposit_result_rows");
            if (!rows.isArray()) continue;

            JsonNode row = rows.get(0);
            String bankName = row.path("bank_name").asText("");
            double rateMin = row.path("rate_min").asDouble(0.0);
            String percentCalculation = row.path("percent_calculation").asText("");
            result.add(new DepositRateDto(bankName, rateMin, percentCalculation));
            parsedCount++;
        }
        logger.debug("Parsed {} banks from one page", parsedCount);
    }
}