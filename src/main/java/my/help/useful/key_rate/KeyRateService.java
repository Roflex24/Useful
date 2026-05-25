package my.help.useful.key_rate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KeyRateService {

    private static final String CBR_URL = "https://www.cbr.ru/hd_base/keyrate/";

    private final KeyRateRepository keyRateRepository;

    public KeyRateModel getKeyRateModel() {
        try {
            // 1. Получаем сегодняшнюю дату
            LocalDate currentDate = LocalDate.now();
            System.out.println("Запрашиваем ставку на дату: " + currentDate);

            // 2. Пытаемся найти запись в БД за сегодня
            Optional<KeyRateEntity> existingEntity = keyRateRepository.findByActualDate(currentDate);

            if (existingEntity.isPresent()) {
                System.out.println("Найдена запись в БД за сегодня: ставка = " + existingEntity.get().getKeyRate());
                KeyRateEntity entity = existingEntity.get();
                return new KeyRateModel(entity.getKeyRate(), entity.getDate());
            }

            // 3. Если в БД нет - парсим сайт ЦБ РФ
            System.out.println("Данных в БД нет, парсим сайт ЦБ РФ...");
            return fetchFromCbrAndSave();

        } catch (Exception e) {
            System.err.println("Ошибка при получении ставки: " + e.getMessage());
            e.printStackTrace();
        }

        return new KeyRateModel(0, "0");
    }

    private KeyRateModel fetchFromCbrAndSave() throws Exception {
        System.out.println("Загрузка страницы: " + CBR_URL);

        Document doc = Jsoup.connect(CBR_URL)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .timeout(15000)
                .get();

        // БЕРЁМ ПЕРВУЮ СТРОКУ ТАБЛИЦЫ (после заголовка)
        Elements rows = doc.select("table.data tr");

        if (rows.size() < 2) {
            throw new RuntimeException("Не найдено строк с данными в таблице");
        }

        // Индекс 1 — это первая строка с данными (индекс 0 — это заголовок <th>)
        Element firstDataRow = rows.get(1);

        String dateStr = firstDataRow.select("td").first().text();
        String rateStr = firstDataRow.select("td").last().text();

        if (rateStr.isEmpty() || dateStr.isEmpty()) {
            throw new RuntimeException("Не удалось найти данные о ставке на странице");
        }

        double rate = Double.parseDouble(rateStr.replace(",", "."));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        LocalDate rateDate = LocalDate.parse(dateStr, formatter);

        System.out.println("Получена ПЕРВАЯ ставка с сайта ЦБ: дата=" + dateStr + ", ставка=" + rate);

        KeyRateEntity entity = new KeyRateEntity();
        entity.setKeyRate(rate);
        entity.setDate(String.valueOf(rateDate));

        KeyRateEntity saved = keyRateRepository.save(entity);
        System.out.println("Сохранена новая запись в БД: ставка = " + saved.getKeyRate());

        return new KeyRateModel(rate, dateStr);
    }

    public List<KeyRateModel> getKeyRateModels() {
        List<KeyRateEntity> entities = keyRateRepository.findAll();
        List<KeyRateModel> models = new ArrayList<>();
        double keyRate = 0;
        for (KeyRateEntity entity : entities) {
            if (entity.getKeyRate() != keyRate) {
                models.add(new KeyRateModel(entity.getKeyRate(), entity.getDate()));
                keyRate = entity.getKeyRate();
            }
        }
        return models;
    }
}