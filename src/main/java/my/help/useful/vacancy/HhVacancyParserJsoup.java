package my.help.useful.vacancy;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.regex.Pattern;

public class HhVacancyParserJsoup {

    private static final String URL = "https://nn.hh.ru/search/vacancy?text=Java+developer&from=suggest_post&salary=&ored_clusters=true&search_field=name&suggestId=8fff2cb4-38a4-45c9-9ac8-93435a4903c2&hhtmFrom=vacancy_search_list&hhtmFromLabel=vacancy_search_line";
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36";

    public static int getVacancyCount() {

        int vacancyCount = 0;
        try {
            // Подключаемся с таймаутом и корректным User-Agent
            Document doc = Jsoup.connect(URL)
                    .userAgent(USER_AGENT)
                    .timeout(10000)
                    .get();

            // Ищем элемент с количеством вакансий
            // Способ 1: через data-qa атрибут
            Elements titleElement = doc.select("[data-qa=title]");
            String titleText = titleElement.text();

            vacancyCount = extractNumberFromText(titleText);

            System.out.println("Поисковый запрос: Java разработчик");
            System.out.println("Найдено вакансий: " + vacancyCount);

        } catch (IOException e) {
            System.err.println("Ошибка при подключении: " + e.getMessage());
        }
        return vacancyCount;
    }

    private static int extractNumberFromText(String text) {
        // Ищем паттерн "Найдено X вакансий"
        Pattern pattern = Pattern.compile("(?:Найдена|Найдено)\\s*([\\d\\s]+)\\s+ваканс");
        java.util.regex.Matcher matcher = pattern.matcher(text);

        if (matcher.find()) {
            String numberStr = matcher.group(1).replaceAll("\\s", "");
            try {
                return Integer.parseInt(numberStr);
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }
}