package my.help.finance.invest;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class MoexApiClient {

    private static final Logger logger = LoggerFactory.getLogger(MoexApiClient.class);

    private static final String MOEX_ISS_URL =
            "https://iss.moex.com/iss/engines/stock/markets/bonds/boards/TQOB/securities.json";

    /**
     * Выполняет GET-запрос к MOEX ISS и возвращает тело ответа в виде строки JSON.
     *
     * @return JSON-строка или null, если ответ пустой
     * @throws IOException при ошибках сетевого взаимодействия
     */
    public String fetchRawBondsJson() throws IOException {
        logger.debug("Fetching data from MOEX ISS: {}", MOEX_ISS_URL);
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(MOEX_ISS_URL);
            return httpClient.execute(request, response -> {
                if (response.getEntity() == null) {
                    return null;
                }
                return EntityUtils.toString(response.getEntity());
            });
        }
    }
}