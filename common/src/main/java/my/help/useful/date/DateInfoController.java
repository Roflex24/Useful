package my.help.useful.date;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;

@RestController
public class DateInfoController {

    @GetMapping("/date/info")
    public ResponseEntity<DateInfo> getDateInfo() {

        // Берем текущую системную дату
        LocalDate date = LocalDate.now();

        // 1. Дата в читаемом формате
        String formattedDate = date.format(DateTimeFormatter.ofPattern("dd MMMM yyyy"));

        // 2. День недели (на русском)
        String dayOfWeek = date.getDayOfWeek().getDisplayName(TextStyle.FULL, new Locale("ru"));

        // 3. Знак зодиака
        String zodiac = getZodiacSign(date);

        return ResponseEntity.ok(
                new DateInfo(formattedDate, dayOfWeek, zodiac)
        );
    }

    private static String getZodiacSign(LocalDate date) {
        int day = date.getDayOfMonth();
        int month = date.getMonthValue();

        return switch (month) {
            case 1 -> (day <= 19) ? "Козерог" : "Водолей";
            case 2 -> (day <= 18) ? "Водолей" : "Рыбы";
            case 3 -> (day <= 20) ? "Рыбы" : "Овен";
            case 4 -> (day <= 19) ? "Овен" : "Телец";
            case 5 -> (day <= 20) ? "Телец" : "Близнецы";
            case 6 -> (day <= 20) ? "Близнецы" : "Рак";
            case 7 -> (day <= 22) ? "Рак" : "Лев";
            case 8 -> (day <= 22) ? "Лев" : "Дева";
            case 9 -> (day <= 22) ? "Дева" : "Весы";
            case 10 -> (day <= 22) ? "Весы" : "Скорпион";
            case 11 -> (day <= 21) ? "Скорпион" : "Стрелец";
            case 12 -> (day <= 21) ? "Стрелец" : "Козерог";
            default -> "";
        };
    }
}
