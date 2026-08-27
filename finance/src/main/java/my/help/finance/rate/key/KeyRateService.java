package my.help.finance.rate.key;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import javax.net.ssl.*;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class KeyRateService {

    private static final String CBR_URL = "https://www.cbr.ru/hd_base/keyrate/";

    private final KeyRateRepository keyRateRepository;

    public KeyRateRs get() {
        try {
            LocalDate currentDate = LocalDate.now();
            log.info("Запрашиваем ставку на дату: {}", currentDate);

            Optional<KeyRate> existingEntity = keyRateRepository.findById(currentDate);

            if (existingEntity.isPresent()) {
                System.out.println("Найдена запись в БД за сегодня: ставка = " + existingEntity.get().getKeyRate());
                KeyRate entity = existingEntity.get();
                return new KeyRateRs(entity.getKeyRate(), entity.getDate());
            }

            log.info("Данных в БД нет, парсим сайт ЦБ РФ...");
            return fetchFromCbrAndSave(currentDate);

        } catch (Exception e) {
            System.err.println("Ошибка при получении ставки: " + e.getMessage());
            e.printStackTrace();
        }

        return new KeyRateRs(0, null);
    }

    public List<KeyRateRs> getList() {
        List<KeyRate> entities = keyRateRepository.findAll();
        List<KeyRateRs> models = new ArrayList<>();
        double keyRate = 0;
        for (KeyRate entity : entities) {
            if (entity.getKeyRate() != keyRate) {
                models.add(new KeyRateRs(entity.getKeyRate(), entity.getDate()));
                keyRate = entity.getKeyRate();
            }
        }
        return models;
    }

    private KeyRateRs fetchFromCbrAndSave(LocalDate date) throws Exception {
        System.out.println("Загрузка страницы: " + CBR_URL);

        // === ВАРИАНТ 1: Отключение проверки SSL (только для разработки) ===
        TrustManager[] trustAllCerts = new TrustManager[] {
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                    }
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                    }
                }
        };

        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, new SecureRandom());

        // Применяем созданный SSLContext только для этого Jsoup-соединения
        Document doc = Jsoup.connect(CBR_URL)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .timeout(15000)
                .sslSocketFactory(sc.getSocketFactory())
                .get();
        // ================================================================

        Elements rows = doc.select("table.data tr");

        if (rows.size() < 2) {
            throw new RuntimeException("Не найдено строк с данными в таблице");
        }

        Element firstDataRow = rows.get(1);

        String dateStr = Objects.requireNonNull(firstDataRow.select("td").first()).text();
        String rateStr = Objects.requireNonNull(firstDataRow.select("td").last()).text();

        if (rateStr.isEmpty() || dateStr.isEmpty()) {
            throw new RuntimeException("Не удалось найти данные о ставке на странице");
        }

        double rate = Double.parseDouble(rateStr.replace(",", "."));

        System.out.println("Получена ПЕРВАЯ ставка с сайта ЦБ: дата=" + dateStr + ", ставка=" + rate);

        KeyRate entity = new KeyRate();
        entity.setKeyRate(rate);
        entity.setDate(date);

        KeyRate saved = keyRateRepository.save(entity);
        System.out.println("Сохранена новая запись в БД: ставка = " + saved.getKeyRate());

        return new KeyRateRs(rate, date);
    }
}