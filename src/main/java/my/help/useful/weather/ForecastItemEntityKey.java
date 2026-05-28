package my.help.useful.weather;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.time.LocalDateTime;

@Embeddable
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ForecastItemEntityKey {

    private LocalDateTime dateTime;
    private String cityNameEn;
}
