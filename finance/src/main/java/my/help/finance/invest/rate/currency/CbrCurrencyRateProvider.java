package my.help.finance.invest.rate.currency;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;

@Service
@RequiredArgsConstructor
@Slf4j
public class CbrCurrencyRateProvider {

    private static final String CBR_URL = "http://www.cbr.ru/scripts/XML_daily.asp";
    private final CurrencyRateRepository currencyRateRepository;

    public CurrencyRateRs get() throws Exception {
        log.info("Getting current currency rates for today");
        LocalDate today = LocalDate.now();
        Optional<CurrencyRate> currencyRateEntityOptional = currencyRateRepository.findByActualDate(today);

        if (currencyRateEntityOptional.isPresent()) {
            CurrencyRate currencyRate = currencyRateEntityOptional.get();
            log.info("Found existing rates in DB: USD={}, EUR={}, date={}",
                    currencyRate.getUsdRate(), currencyRate.getEurRate(), currencyRate.getActualDate());
            return new CurrencyRateRs(currencyRate.getUsdRate(), currencyRate.getEurRate(), currencyRate.getActualDate());
        } else {
            log.info("No rates found in DB for today, fetching from CBR");
            try {
                double usdRate = Math.round(getRateByCharCode("USD") * 100.0) / 100.0;
                double eurRate = Math.round(getRateByCharCode("EUR") * 100.0) / 100.0;
                log.debug("Fetched rates: USD={}, EUR={}", usdRate, eurRate);
                LocalDate date = LocalDate.now();
                currencyRateRepository.save(new CurrencyRate(date, usdRate, eurRate));
                log.info("Saved new rates to DB: USD={}, EUR={}, date={}", usdRate, eurRate, date);
                return new CurrencyRateRs(usdRate, eurRate, date);
            } catch (Exception e) {
                log.error("Failed to fetch or save currency rates", e);
                throw e; // или обернуть в RuntimeException
            }
        }
    }

    public List<CurrencyRateRs> getList() {
        log.info("Retrieving all currency rate history");
        List<CurrencyRate> currencyRateEntities = currencyRateRepository.findAll();
        List<CurrencyRateRs> currencyRateRs = new ArrayList<>();
        for (CurrencyRate currencyRate : currencyRateEntities) {
            currencyRateRs.add(new CurrencyRateRs(currencyRate.getUsdRate(), currencyRate.getEurRate(), currencyRate.getActualDate()));
        }
        log.debug("Returning {} historical records", currencyRateRs.size());
        return currencyRateRs;
    }

    private double getRateByCharCode(String charCode) throws Exception {
        log.debug("Fetching rate for currency: {}", charCode);
        // Формируем URL с актуальной датой
        String dateParam = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        String url = CBR_URL + "?date_req=" + dateParam;
        log.debug("Request URL: {}", url);

        // Создаем HTTP клиент и отправляем запрос
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        log.debug("Received response with status: {}", response.statusCode());

        if (response.statusCode() != 200) {
            log.error("CBR responded with non-200 status: {}", response.statusCode());
            throw new RuntimeException("HTTP error: " + response.statusCode());
        }

        // Парсим XML
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        Document doc = factory.newDocumentBuilder().parse(new java.io.ByteArrayInputStream(response.body().getBytes()));

        // Ищем элемент Valute с нужным CharCode
        NodeList valuteNodes = doc.getElementsByTagName("Valute");
        for (int i = 0; i < valuteNodes.getLength(); i++) {
            var valute = valuteNodes.item(i);
            var charCodeNode = ((org.w3c.dom.Element) valute).getElementsByTagName("CharCode").item(0);
            if (charCodeNode.getTextContent().equals(charCode)) {
                var valueNode = ((org.w3c.dom.Element) valute).getElementsByTagName("Value").item(0);
                String valueStr = valueNode.getTextContent();
                // Заменяем запятую на точку для парсинга Double
                double rate = Double.parseDouble(valueStr.replace(',', '.'));
                log.debug("Rate for {} = {}", charCode, rate);
                return rate;
            }
        }
        log.error("Currency {} not found in CBR response", charCode);
        throw new IllegalArgumentException("Currency not found: " + charCode);
    }
}