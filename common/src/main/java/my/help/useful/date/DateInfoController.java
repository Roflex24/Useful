package my.help.useful.date;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;

@RestController
@Tag(name = "Common API", description = "Общее")
public class DateInfoController {

    @GetMapping("/api/date/info")
    public DateInfo getDateInfo() {

        LocalDate date = LocalDate.now();
        String formattedDate = date.format(DateTimeFormatter.ofPattern("dd MMMM yyyy"));
        String dayOfWeek = date.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.forLanguageTag("ru"));
        String zodiac = getZodiacSign(date);

        return new DateInfo(formattedDate, dayOfWeek, zodiac);
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
