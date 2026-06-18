package my.help.useful.weather;

import lombok.*;

import java.time.LocalDateTime;

// Класс, представляющий один прогноз на определенное время
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ForecastItem {

    private LocalDateTime dateTime;      // Полная дата и время
    private String date;          // Только дата
    private String time;          // Только время
    private double temperature;   // Температура
    private String description;   // Описание погоды
    private String mainWeather;   // Основная погода (Clouds, Rain и т.д.)
    private int humidity;         // Влажность
    private double windSpeed;     // Скорость ветра
    private int pressure;         // Давление

}