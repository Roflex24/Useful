package my.help.useful.weather;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.util.Timeout;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class ForecastService {

    private static final String API_KEY = "0433ec9580b974ab9f33aa498a8c89b0";
    private static final String FORECAST_URL = "https://api.openweathermap.org/data/2.5/forecast";

    private final ObjectMapper objectMapper;
    private final WeatherForecastRepository weatherForecastRepository;
    private final WeatherMapping weatherMapping;

    public ForecastService(WeatherForecastRepository weatherForecastRepository) {
        this.weatherForecastRepository = weatherForecastRepository;
        this.objectMapper = new ObjectMapper();
        this.weatherMapping = new WeatherMapping();
    }

    @Transactional
    public WeatherForecast getWeatherForecast(String cityNameEn) {
        WeatherForecast weatherForecast;
        try {
            String forecastData = getForecast(cityNameEn);
            weatherForecast = parseForecastToObject(forecastData);
            
            weatherForecastRepository.deleteByIdCityNameEn(cityNameEn);
            weatherForecastRepository.saveAll(weatherMapping.getWeatherForecastEntity(weatherForecast, cityNameEn));
            weatherForecast.setActual(true);
        } catch (Exception e) {
            System.err.println("Ошибка: " + e.getMessage());
            System.err.println("Берем данные из бд");
            weatherForecastRepository.deleteAllByIdDateTimeBefore(LocalDateTime.now().minusHours(2));
            weatherForecast = weatherMapping.getWeatherForecast(weatherForecastRepository.findByIdCityNameEn(cityNameEn));
        }
        return weatherForecast;
    }

    public ForecastItemRs getForecastItemRs(String city) {
        WeatherForecast weatherForecast;
        ForecastItemRs forecastItemRs = new ForecastItemRs();

        try {
            // Создаем объект с информацией о погоде
            weatherForecast = getWeatherForecast(city);
            forecastItemRs.setActual(weatherForecast.isActual());
            forecastItemRs.setForecastItem(weatherForecast.getForecastItems().getFirst());

        } catch (Exception e) {
            System.err.println("Ошибка: " + e.getMessage());
        }
        return forecastItemRs;
    }


    private String getForecast(String city) throws Exception {
        String url = String.format("%s?q=%s&appid=%s&units=metric&lang=ru&cnt=40",
                FORECAST_URL, city, API_KEY);

        try (CloseableHttpClient client = HttpClients.custom()
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setConnectionRequestTimeout(Timeout.ofSeconds(2))   // ожидание соединения из пула
                        .setConnectTimeout(Timeout.ofSeconds(2))            // установка TCP соединения (deprecated, но работает)
                        .setResponseTimeout(Timeout.ofSeconds(2))          // ожидание данных после соединения
                        .build())
                .build()) {

            HttpGet request = new HttpGet(url);
            try (CloseableHttpResponse response = client.execute(request)) {
                return EntityUtils.toString(response.getEntity());
            }
        }
    }

    // Метод для парсинга JSON в объект WeatherForecast
    private WeatherForecast parseForecastToObject(String jsonData) throws Exception {
        JsonNode root = objectMapper.readTree(jsonData);
        JsonNode list = root.path("list");

        WeatherForecast weatherForecast = new WeatherForecast();

        // Заполняем информацию о городе
        weatherForecast.setCityName(root.path("city").path("name").asText());

        // Список для хранения прогнозов
        List<ForecastItem> forecastItems = new ArrayList<>();

        for (JsonNode forecast : list) {
            ForecastItem item = new ForecastItem();

            // Дата и время
            String dtTxt = forecast.path("dt_txt").asText();
            item.setDateTime(LocalDateTime.parse(dtTxt, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            item.setDate(dtTxt.split(" ")[0]);
            item.setTime(dtTxt.split(" ")[1].substring(0, 5));

            // Температура
            item.setTemperature(forecast.path("main").path("temp").asDouble());

            // Погодные условия
            item.setDescription(forecast.path("weather").get(0).path("description").asText());
            item.setMainWeather(forecast.path("weather").get(0).path("main").asText());

            // Влажность
            item.setHumidity(forecast.path("main").path("humidity").asInt());

            // Ветер
            item.setWindSpeed(forecast.path("wind").path("speed").asDouble());

            // Давление (опционально)
            item.setPressure(forecast.path("main").path("pressure").asInt());

            forecastItems.add(item);
        }

        weatherForecast.setForecastItems(forecastItems);


        return weatherForecast;
    }
}
