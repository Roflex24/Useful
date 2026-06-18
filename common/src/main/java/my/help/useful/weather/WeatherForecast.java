package my.help.useful.weather;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

// Класс, представляющий прогноз погоды в целом
@Setter
@Getter
public class WeatherForecast {
    // Геттеры и сеттеры
    private String cityName;
    private boolean isActual;
    private List<ForecastItem> forecastItems;

    // Конструкторы
    public WeatherForecast() {
        this.forecastItems = new ArrayList<>();
    }

    public WeatherForecast(String cityName, String country, List<ForecastItem> forecastItems) {
        this.cityName = cityName;
        this.forecastItems = forecastItems;
    }

    @Override
    public String toString() {
        return String.format("WeatherForecast{city='%s', items=%d}",
                cityName, forecastItems.size());
    }
}