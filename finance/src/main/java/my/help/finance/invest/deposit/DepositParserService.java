package my.help.finance.invest.deposit;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
public class DepositParserService {

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

    public DepositRatesResponse fetchDepositRates() {
        List<DepositRateDto> rates = new ArrayList<>();
        int totalBanks;

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        try {
            // Запрос первой страницы для получения total
            String firstPageUrl = API_URL_BASE + "1";
            HttpResponse<String> firstResponse = sendRequest(client, firstPageUrl);
            if (firstResponse.statusCode() != 200) {
                throw new RuntimeException("HTTP error: " + firstResponse.statusCode());
            }

            JsonNode firstRoot = objectMapper.readTree(firstResponse.body());
            totalBanks = firstRoot.path("total").asInt();          // общее количество банков
            int perPage = firstRoot.path("per_page").asInt();      // групп на странице
            int pages = (int) Math.ceil((double) totalBanks / perPage);

            // Парсим первую страницу
            parsePage(firstRoot, rates);

            // Остальные страницы
            for (int page = 2; page <= pages; page++) {
                String url = API_URL_BASE + page;
                HttpResponse<String> response = sendRequest(client, url);
                if (response.statusCode() != 200) {
                    throw new RuntimeException("HTTP error: " + response.statusCode());
                }
                JsonNode root = objectMapper.readTree(response.body());
                parsePage(root, rates);
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch data from banki.ru", e);
        }

        return new DepositRatesResponse(totalBanks, rates);
    }

    private HttpResponse<String> sendRequest(HttpClient client, String url) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(15))
                .header("Accept", "application/json")
                .header("User-Agent", "Mozilla/5.0")
                .GET()
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private void parsePage(JsonNode root, List<DepositRateDto> result) {
        JsonNode groupedTable = root.path("grouped_table");
        if (!groupedTable.isArray()) return;

        for (JsonNode bankGroup : groupedTable) {
            JsonNode rows = bankGroup.path("deposit_result_rows");
            if (!rows.isArray()) continue;

            JsonNode row = rows.get(0);
            String bankName = row.path("bank_name").asText("");
            double rateMin = row.path("rate_min").asDouble(0.0);
            result.add(new DepositRateDto(bankName, rateMin));
        }
    }
}