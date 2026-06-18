package my.help.useful.weather;

import jakarta.persistence.*;
import lombok.*;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "weather_forecasts")
public class ForecastItemEntity {

    @EmbeddedId
    private ForecastItemEntityKey id;      // Полная дата и время

    private String date;          // Только дата
    private String time;          // Только время
    private double temperature;   // Температура
    private String description;   // Описание погоды
    private String mainWeather;   // Основная погода (Clouds, Rain и т.д.)
    private int humidity;         // Влажность
    private double windSpeed;     // Скорость ветра
    private int pressure;
    private String cityNameRu;
}
