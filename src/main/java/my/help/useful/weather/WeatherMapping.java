package my.help.useful.weather;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class WeatherMapping {

    public List<ForecastItemEntity> getWeatherForecastEntity(WeatherForecast weatherForecast, String cityNameEn) {



        List<ForecastItemEntity> forecastItemEntities = new ArrayList<>();
        List<ForecastItem> forecastItems = weatherForecast.getForecastItems();

        for (ForecastItem forecastItem : forecastItems) {
            ForecastItemEntity forecastItemEntity = new ForecastItemEntity();

            forecastItemEntity.setMainWeather(forecastItem.getMainWeather());
            forecastItemEntity.setDescription(forecastItem.getDescription());
            forecastItemEntity.setId(new ForecastItemEntityKey(forecastItem.getDateTime(), cityNameEn));
            forecastItemEntity.setDate(forecastItem.getDate());
            forecastItemEntity.setTime(forecastItem.getTime());
            forecastItemEntity.setHumidity(forecastItem.getHumidity());
            forecastItemEntity.setPressure(forecastItem.getPressure());
            forecastItemEntity.setWindSpeed(forecastItem.getWindSpeed());
            forecastItemEntity.setTemperature(forecastItem.getTemperature());
            forecastItemEntity.setCityNameRu(weatherForecast.getCityName());


            forecastItemEntities.add(forecastItemEntity);
        }
        return forecastItemEntities;
    }


    public WeatherForecast getWeatherForecast(List<ForecastItemEntity> forecastItemEntityList) {
        if (forecastItemEntityList == null || forecastItemEntityList.isEmpty()) {
            return new WeatherForecast();
        }

        WeatherForecast weatherForecast = new WeatherForecast();
        weatherForecast.setCityName(forecastItemEntityList.getFirst().getCityNameRu());

        List<ForecastItem> forecastItems = new ArrayList<>();

        for (ForecastItemEntity forecastItemEntity : forecastItemEntityList) {
            ForecastItem forecastItem = new ForecastItem();

            forecastItem.setMainWeather(forecastItemEntity.getMainWeather());
            forecastItem.setDate(forecastItemEntity.getDate());
            forecastItem.setDescription(forecastItemEntity.getDescription());
            forecastItem.setDateTime(forecastItemEntity.getId().getDateTime());
            forecastItem.setTime(forecastItemEntity.getTime());
            forecastItem.setHumidity(forecastItemEntity.getHumidity());
            forecastItem.setPressure(forecastItemEntity.getPressure());
            forecastItem.setWindSpeed(forecastItemEntity.getWindSpeed());
            forecastItem.setTemperature(forecastItemEntity.getTemperature());

            forecastItems.add(forecastItem);
        }

        weatherForecast.setForecastItems(forecastItems);
        return weatherForecast;
    }
}
