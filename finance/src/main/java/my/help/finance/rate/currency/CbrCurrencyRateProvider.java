package my.help.finance.rate.currency;

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
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;

@Service
@RequiredArgsConstructor
public class CbrCurrencyRateProvider {

    private static final String CBR_URL = "http://www.cbr.ru/scripts/XML_daily.asp";
    private final CurrencyRateRepository currencyRateRepository;

    public CurrencyRateRs get() throws Exception {
        Optional<CurrencyRate> currencyRateEntityOptional = currencyRateRepository.findByActualDate(LocalDate.now());

        if (currencyRateEntityOptional.isPresent()) {
            CurrencyRate currencyRate = currencyRateEntityOptional.get();
            return new CurrencyRateRs(currencyRate.getUsdRate(), currencyRate.getEurRate(), currencyRate.getActualDate());
        } else {
            double usdRate = Math.round(getRateByCharCode("USD") * 100.0) / 100.0;
            double eurRate = Math.round(getRateByCharCode("EUR") * 100.0) / 100.0;
            LocalDate date = LocalDate.now();
            currencyRateRepository.save(new CurrencyRate(date, usdRate, eurRate));
            return new CurrencyRateRs(usdRate, eurRate, date);
        }
    }

    public List<CurrencyRateRs> getList() {
        List<CurrencyRate> currencyRateEntities = currencyRateRepository.findAll();
        List<CurrencyRateRs> currencyRateRs = new ArrayList<>();
        for (CurrencyRate currencyRate : currencyRateEntities) {
            currencyRateRs.add(new CurrencyRateRs(currencyRate.getUsdRate(), currencyRate.getEurRate(), currencyRate.getActualDate()));
        }
        return currencyRateRs;
    }

    private double getRateByCharCode(String charCode) throws Exception {
        // Формируем URL с актуальной датой
        String dateParam = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        String url = CBR_URL + "?date_req=" + dateParam;

        // Создаем HTTP клиент и отправляем запрос
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

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
                return Double.parseDouble(valueStr.replace(',', '.'));
            }
        }
        throw new IllegalArgumentException("Currency not found: " + charCode);
    }

}