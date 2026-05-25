package my.help.useful.weather;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/weather")
public class WeatherController {

    private final ForecastService forecastConsumer;

    public WeatherController(ForecastService forecastConsumer) {
        this.forecastConsumer = forecastConsumer;
    }

    @GetMapping()
    public ResponseEntity<WeatherForecast> getWeather(@RequestParam("city") String city) {
        return ResponseEntity.ok(forecastConsumer.getWeatherForecast(city));
    }

    @GetMapping("/now")
    public ResponseEntity<ForecastItemRs> getWeatherNow(@RequestParam("city") String city) {
        return ResponseEntity.ok(forecastConsumer.getForecastItemRs(city));
    }
}
