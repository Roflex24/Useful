package my.help.useful.weather;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Embeddable
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ForecastItemEntityKey {

    private LocalDateTime dateTime;
    private String cityNameEn;
}
