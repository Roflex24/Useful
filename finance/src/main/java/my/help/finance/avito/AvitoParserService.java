package my.help.finance.avito;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Сервис парсинга HTML-страниц Авито.
 *
 * Зависимость (Maven):
 * <dependency>
 *     <groupId>org.jsoup</groupId>
 *     <artifactId>jsoup</artifactId>
 *     <version>1.17.2</version>
 * </dependency>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AvitoParserService {

    private static final String STOP_MARKER = "Похоже на то, что вы ищете";
    private static final String AVITO_BASE  = "https://www.avito.ru";

    private final ApartmentRepository repository;

    // ------------------------------------------------------------------
    // Публичный API
    // ------------------------------------------------------------------

    /**
     * Обрабатывает ОДИН HTML (оставлен для обратной совместимости).
     */
    @Transactional
    public List<Apartment> parseAndSave(String htmlContent) {
        return parseAndSaveMultiple(List.of(htmlContent));
    }

    /**
     * Обрабатывает НЕСКОЛЬКО HTML-файлов за одну транзакцию.
     * Дубли (одинаковый avitoId), встретившиеся в разных файлах или
     * внутри одного файла, схлопываются в памяти ДО обращения к БД —
     * побеждает последнее по порядку вхождение.
     * Затем для каждого уникального avitoId делается upsert:
     * если запись уже есть в базе — обновляется, если нет — создаётся.
     */
    @Transactional
    public List<Apartment> parseAndSaveMultiple(List<String> htmlContents) {
        Map<String, Apartment> deduped = new LinkedHashMap<>();
        int totalParsed = 0;

        for (String html : htmlContents) {
            List<Apartment> parsedFromFile = parseHtml(html);
            totalParsed += parsedFromFile.size();
            for (Apartment apt : parsedFromFile) {
                deduped.put(apt.getAvitoId(), apt);
            }
        }

        List<Apartment> saved = new ArrayList<>();
        for (Apartment apt : deduped.values()) {
            saved.add(upsert(apt));
        }

        log.info("Files: {}, raw items parsed: {}, unique after dedup: {}, saved/updated: {}.",
                htmlContents.size(), totalParsed, deduped.size(), saved.size());

        return saved;
    }

    // ------------------------------------------------------------------
    // Приватные методы
    // ------------------------------------------------------------------

    private List<Apartment> parseHtml(String html) {
        int stopIdx = html.indexOf(STOP_MARKER);
        String cleanHtml = (stopIdx != -1) ? html.substring(0, stopIdx) : html;

        // Реальные координаты (lat/lng) не выведены в видимую разметку,
        // но встроены Авито в служебный JSON внутри <script> на той же
        // странице — достаём их оттуда и сопоставляем с ID объявлений.
        Map<String, double[]> coordsByItemId = extractCoordinatesByItemId(cleanHtml);

        Document doc = Jsoup.parse(cleanHtml);
        Elements items = doc.select("[data-marker=item]");

        List<Apartment> result = new ArrayList<>();
        for (Element el : items) {
            parseItem(el, coordsByItemId).ifPresent(result::add);
        }
        return result;
    }

    /**
     * Строит соответствие "ID объявления → [широта, долгота]", разбирая
     * служебный JSON, который Авито встраивает в HTML страницы каталога
     * (используется картой на самой Авито). Формат внутри JSON:
     *   ...,"debug":{"id":8144067665}},...,"coords":{"lat":"56.23...","lng":"43.86..."...
     * ID и координаты объявления всегда лежат в одном и том же вложенном
     * объекте, поэтому для каждого найденного "coords" берём ближайший
     * ПРЕДШЕСТВУЮЩИЙ по тексту "debug":{"id":...} — это и есть ID
     * того объявления, которому эти координаты принадлежат.
     */
    private Map<String, double[]> extractCoordinatesByItemId(String html) {
        Map<String, double[]> result = new HashMap<>();

        Pattern idPattern     = Pattern.compile("\"debug\":\\{\"id\":(\\d+)\\}");
        Pattern coordsPattern = Pattern.compile("\"coords\":\\{\"lat\":\"([^\"]+)\",\"lng\":\"([^\"]+)\"");

        List<Integer> idPositions = new ArrayList<>();
        List<String> idValues = new ArrayList<>();
        Matcher idMatcher = idPattern.matcher(html);
        while (idMatcher.find()) {
            idPositions.add(idMatcher.start());
            idValues.add(idMatcher.group(1));
        }

        Matcher coordsMatcher = coordsPattern.matcher(html);
        while (coordsMatcher.find()) {
            int coordsPos = coordsMatcher.start();

            // Бинарный поиск последней позиции id, которая меньше coordsPos
            int lo = 0, hi = idPositions.size() - 1, matchIdx = -1;
            while (lo <= hi) {
                int mid = (lo + hi) / 2;
                if (idPositions.get(mid) < coordsPos) {
                    matchIdx = mid;
                    lo = mid + 1;
                } else {
                    hi = mid - 1;
                }
            }

            if (matchIdx != -1) {
                try {
                    double lat = Double.parseDouble(coordsMatcher.group(1));
                    double lng = Double.parseDouble(coordsMatcher.group(2));
                    result.put(idValues.get(matchIdx), new double[]{ lat, lng });
                } catch (NumberFormatException ignored) {
                    // если координата в неожиданном формате — просто пропускаем
                }
            }
        }

        return result;
    }

    /** Парсит один элемент объявления в объект Apartment. */
    private Optional<Apartment> parseItem(Element el, Map<String, double[]> coordsByItemId) {
        String avitoId = el.attr("data-item-id");
        if (avitoId.isBlank()) return Optional.empty();

        String title    = text(el, "[data-marker=item-title]");
        String priceRaw = text(el, "[data-marker=item-price]");

        // Адрес состоит из 1-2 строк внутри [data-marker=item-location]:
        //   1-я строка — улица и дом ("ул. Старых Производственников, 11")
        //   2-я строка (не всегда есть) — метро с временем в пути
        //     ("Парк Культуры, 16–20 мин.") либо название района ("р-н Приокский")
        String[] addressLines = parseAddressLines(el);
        String address = addressLines[0];
        String metro   = addressLines[1];

        Element descEl = el.selectFirst("[itemprop=description]");
        String description = (descEl != null) ? descEl.attr("content") : null;

        String imageUrl = extractImageUrl(el);

        Element urlEl = el.selectFirst("a[itemprop=url]");
        String url = null;
        if (urlEl != null) {
            String href = urlEl.attr("href");
            url = href.startsWith("http") ? href : AVITO_BASE + href;
        }

        double[] coords = coordsByItemId.get(avitoId);
        Double latitude  = (coords != null) ? coords[0] : null;
        Double longitude = (coords != null) ? coords[1] : null;

        Apartment apt = Apartment.builder()
                .avitoId(avitoId)
                .title(title)
                .priceRaw(priceRaw)
                .price(cleanPrice(priceRaw))
                .address(address)
                .metro(metro)
                .latitude(latitude)
                .longitude(longitude)
                .description(description)
                .url(url)
                .imageUrl(imageUrl)
                .build();

        return Optional.of(apt);
    }

    /**
     * Извлекает URL главной фотографии объявления.
     * Поддерживает два формата разметки Авито:
     *   1) Старый: элемент с itemprop="image" и атрибутом src.
     *   2) Новый (актуальный): реальный URL картинки закодирован прямо
     *      в атрибуте data-marker первого слайда фотослайдера —
     *      data-marker="slider-image/image-&lt;URL&gt;". Тег &lt;img&gt; с src
     *      в статичном HTML отсутствует (картинки лениво подгружаются JS),
     *      поэтому URL приходится доставать из этого маркера.
     */
    private String extractImageUrl(Element el) {
        Element imgEl = el.selectFirst("[itemprop=image]");
        if (imgEl != null) {
            String src = imgEl.attr("src");
            if (!src.isBlank()) return src;
        }

        Element sliderImg = el.selectFirst("[data-marker^=slider-image/image-]");
        if (sliderImg != null) {
            String marker = sliderImg.attr("data-marker");
            String prefix = "slider-image/image-";
            int idx = marker.indexOf(prefix);
            if (idx != -1) {
                String url = marker.substring(idx + prefix.length());
                if (!url.isBlank()) return url;
            }
        }

        return null;
    }

    /**
     * Извлекает адрес и метро/район как отдельные строки.
     * Внутри блока [data-marker=item-address] есть вложенный
     * [data-marker=item-location] с одним или двумя дочерними &lt;p&gt;:
     *   - если один &lt;p&gt; — это просто адрес без метро/района;
     *   - если два &lt;p&gt; — первый это адрес, второй метро/район.
     *
     * @return массив из двух элементов: [address, metro] (любой может быть null)
     */
    private String[] parseAddressLines(Element el) {
        Element location = el.selectFirst("[data-marker=item-address] [data-marker=item-location]");
        if (location == null) {
            return new String[]{ null, null };
        }

        Elements paragraphs = location.select("> p");
        String address = paragraphs.size() > 0 ? cleanText(paragraphs.get(0).text()) : null;
        String metro   = paragraphs.size() > 1 ? cleanText(paragraphs.get(1).text()) : null;

        return new String[]{ address, metro };
    }

    /** Upsert: обновляет существующую запись или создаёт новую. */
    private Apartment upsert(Apartment incoming) {
        return repository.findByAvitoId(incoming.getAvitoId())
                .map(existing -> {
                    existing.setTitle(incoming.getTitle());
                    existing.setPrice(incoming.getPrice());
                    existing.setPriceRaw(incoming.getPriceRaw());
                    existing.setAddress(incoming.getAddress());
                    existing.setMetro(incoming.getMetro());
                    existing.setLatitude(incoming.getLatitude());
                    existing.setLongitude(incoming.getLongitude());
                    existing.setDescription(incoming.getDescription());
                    existing.setUrl(incoming.getUrl());
                    existing.setImageUrl(incoming.getImageUrl());
                    return repository.save(existing);
                })
                .orElseGet(() -> repository.save(incoming));
    }

    // ------------------------------------------------------------------
    // Вспомогательные методы
    // ------------------------------------------------------------------

    /** Возвращает очищенный текст первого найденного элемента. */
    private String text(Element parent, String cssSelector) {
        Element el = parent.selectFirst(cssSelector);
        if (el == null) return null;
        return cleanText(el.text());
    }

    /** Убирает неразрывные пробелы и обрезает по краям; пустую строку превращает в null. */
    private String cleanText(String raw) {
        if (raw == null) return null;
        String t = raw.replace("\u00A0", " ").trim();
        return t.isEmpty() ? null : t;
    }

    /**
     * Преобразует строку цены "13 146 300 ₽" в Long.
     * Удаляет все нецифровые символы.
     */
    private Long cleanPrice(String priceRaw) {
        if (priceRaw == null || priceRaw.isBlank()) return null;
        String digits = priceRaw.replaceAll("[^\\d]", "");
        if (digits.isEmpty()) return null;
        try {
            return Long.parseLong(digits);
        } catch (NumberFormatException e) {
            log.warn("Cannot parse price: {}", priceRaw);
            return null;
        }
    }
}