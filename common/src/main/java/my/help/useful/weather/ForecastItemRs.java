package my.help.useful.weather;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ForecastItemRs {

    private boolean isActual;
    private ForecastItem forecastItem;
}
