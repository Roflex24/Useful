package my.help.finance.avito.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.help.finance.avito.entity.Apartment;
import my.help.finance.avito.entity.ApartmentBadge;
import my.help.finance.avito.entity.ApartmentImage;
import my.help.finance.avito.repository.ApartmentBadgeRepository;
import my.help.finance.avito.repository.ApartmentImageRepository;
import my.help.finance.avito.repository.ApartmentRepository;
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
 * Сервис парсинга HTML-страниц выдачи Авито.
 * <p>
 * Извлекает из каждой карточки [data-marker=item] максимум того, что
 * реально есть в разметке страницы поиска: структурные параметры из
 * заголовка (комнаты/площадь/этаж), полную цену и цену за м², адрес
 * по частям (улица, дом, метро/район с временем в пути), координаты,
 * полное описание, ВСЕ фотографии, бейджи объявления и продавца,
 * данные продавца, статус "новое"/"продвигается" и дату публикации.
 * <p>
 * Зависимости (Maven):
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

    // Заголовок вида "3-к. квартира, 66,7 м², 9/9 эт." или "Студия, 25 м², 3/9 эт."
    private static final Pattern TITLE_ROOMS  = Pattern.compile("^(\\d+)-к\\.");
    private static final Pattern TITLE_STUDIO = Pattern.compile("(?i)студия");
    private static final Pattern TITLE_AREA   = Pattern.compile("([\\d]+(?:[.,]\\d+)?)\\s*м²");
    private static final Pattern TITLE_FLOOR  = Pattern.compile("(\\d+)\\s*/\\s*(\\d+)\\s*эт");

    private static final Pattern DIGITS = Pattern.compile("\\d+");

    private final ApartmentRepository repository;
    private final ApartmentImageRepository imageRepository;
    private final ApartmentBadgeRepository badgeRepository;

    // ------------------------------------------------------------------
    // Публичный API
    // ------------------------------------------------------------------

    /**
     * Обрабатывает НЕСКОЛЬКО HTML-файлов за одну транзакцию.
     * Дубли (одинаковый avitoId), встретившиеся в разных файлах или
     * внутри одного файла, схлопываются в памяти ДО обращения к БД —
     * побеждает последнее по порядку вхождение.
     * Затем для каждого уникального avitoId делается upsert:
     * если запись уже есть в базе — обновляется (включая фото и бейджи),
     * если нет — создаётся.
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

    /**
     * Полностью очищает базу квартир (используется кнопкой "Очистить базу").
     * Порядок важен: apartments — родитель для apartment_images и
     * apartment_badges (FK apartment_id NOT NULL), поэтому детей удаляем
     * первыми. deleteAllInBatch() — это прямой SQL DELETE, JPA-каскад при
     * нём не срабатывает (он работает только при удалении через
     * EntityManager/remove()), поэтому удалять родителя первым нельзя —
     * упадёт с нарушением внешнего ключа.
     */
    @Transactional
    public void deleteAllApartments() {
        imageRepository.deleteAllInBatch();
        badgeRepository.deleteAllInBatch();
        repository.deleteAllInBatch();
    }

    /**
     * Удаляет одну квартиру по её avitoId (вместе с фото и бейджами).
     * В отличие от deleteAllApartments(), здесь используется обычный
     * repository.delete() через EntityManager — он корректно каскадирует
     * удаление images/badges (CascadeType.ALL + orphanRemoval на Apartment),
     * так что отдельно чистить дочерние таблицы не нужно.
     *
     * @return true, если квартира была найдена и удалена; false, если её не было
     */
    @Transactional
    public boolean deleteApartmentByAvitoId(String avitoId) {
        return repository.findByAvitoId(avitoId)
                .map(apt -> {
                    repository.delete(apt);
                    return true;
                })
                .orElse(false);
    }

    // ------------------------------------------------------------------
    // Парсинг страницы
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

    // ------------------------------------------------------------------
    // Парсинг одной карточки
    // ------------------------------------------------------------------

    /** Парсит один элемент объявления в объект Apartment со всеми доступными полями. */
    private Optional<Apartment> parseItem(Element el, Map<String, double[]> coordsByItemId) {
        String avitoId = el.attr("data-item-id");
        if (avitoId.isBlank()) return Optional.empty();

        Apartment apt = new Apartment();
        apt.setAvitoId(avitoId);

        String title = text(el, "[data-marker=item-title]");
        apt.setTitle(title);
        parseTitleStructure(title, apt);

        parsePrice(el, apt);
        parseAddress(el, apt);
        parseDescription(el, apt);
        parseImages(el, apt);
        parseUrl(el, apt);
        parseStatusFlags(el, apt);
        parseSeller(el, apt);
        parseBadges(el, apt);

        double[] coords = coordsByItemId.get(avitoId);
        if (coords != null) {
            apt.setLatitude(coords[0]);
            apt.setLongitude(coords[1]);
        }

        return Optional.of(apt);
    }

    /**
     * Достаёт из заголовка ("3-к. квартира, 66,7 м², 9/9 эт." /
     * "Студия, 25 м², 3/9 эт.") количество комнат (или флаг студии),
     * площадь, этаж и этажность дома.
     */
    private void parseTitleStructure(String title, Apartment apt) {
        if (title == null) return;

        Matcher mRooms = TITLE_ROOMS.matcher(title);
        if (mRooms.find()) {
            apt.setRooms(Integer.parseInt(mRooms.group(1)));
            apt.setStudio(false);
        } else if (TITLE_STUDIO.matcher(title).find()) {
            apt.setStudio(true);
        }

        Matcher mArea = TITLE_AREA.matcher(title);
        if (mArea.find()) {
            apt.setTotalArea(parseDoubleSafe(mArea.group(1)));
        }

        Matcher mFloor = TITLE_FLOOR.matcher(title);
        if (mFloor.find()) {
            apt.setFloor(parseIntSafe(mFloor.group(1)));
            apt.setTotalFloors(parseIntSafe(mFloor.group(2)));
        }
    }

    /**
     * Цена берётся из микроразметки schema.org (itemprop="price"/"priceCurrency"),
     * это надёжнее, чем чистить видимый текст. Отдельно, если есть, достаётся
     * цена за квадратный метр (блок ".price-inlineNormalizedPrice...").
     */
    private void parsePrice(Element el, Apartment apt) {
        Element priceMeta = el.selectFirst("[data-marker=item-price] [itemprop=price]");
        if (priceMeta != null) {
            apt.setPrice(parseLongDigits(priceMeta.attr("content")));
        }
        Element currencyMeta = el.selectFirst("[data-marker=item-price] [itemprop=priceCurrency]");
        if (currencyMeta != null) {
            apt.setCurrency(cleanText(currencyMeta.attr("content")));
        }
        apt.setPriceRaw(text(el, "[data-marker=item-price-value]"));

        // Класс у Авито хэшированный ("price-inlineNormalizedPrice-J8EYv"),
        // поэтому ищем по префиксу через contains-селектор.
        Element pricePerMeterEl = el.selectFirst("[class*=inlineNormalizedPrice]");
        if (pricePerMeterEl != null) {
            apt.setPricePerMeter(parseLongDigits(pricePerMeterEl.text()));
        }
    }

    /**
     * Извлекает адрес по частям. Внутри [data-marker=item-address] есть
     * вложенный [data-marker=item-location] с одним или двумя дочерними &lt;p&gt;:
     *   - 1-я строка — улица (street_link) и дом (house_link);
     *   - 2-я строка (не всегда есть) — метро (metro_link) с временем в пути,
     *     либо просто название района, если метро в объявлении не указано.
     */
    private void parseAddress(Element el, Apartment apt) {
        Element location = el.selectFirst("[data-marker=item-address] [data-marker=item-location]");
        if (location == null) return;

        Elements paragraphs = location.select("> p");

        if (!paragraphs.isEmpty()) {
            Element line1 = paragraphs.getFirst();
            apt.setAddress(cleanText(line1.text()));

            Element streetEl = line1.selectFirst("[data-marker=street_link]");
            if (streetEl != null) {
                apt.setStreet(cleanText(streetEl.text()));
                apt.setStreetLink(absoluteUrl(streetEl.attr("href")));
            }
            Element houseEl = line1.selectFirst("[data-marker=house_link]");
            if (houseEl != null) {
                apt.setHouseNumber(cleanText(houseEl.text()));
                apt.setHouseLink(absoluteUrl(houseEl.attr("href")));
            }
        }

        if (paragraphs.size() > 1) {
            Element line2 = paragraphs.get(1);
            String line2Text = cleanText(line2.text());
            apt.setMetro(line2Text);

            Element metroEl = line2.selectFirst("[data-marker=metro_link]");
            if (metroEl != null) {
                String metroName = cleanText(metroEl.text());
                apt.setMetroName(metroName);
                apt.setMetroLink(absoluteUrl(metroEl.attr("href")));

                // Остаток строки после названия станции — это время в пути,
                // например "Горьковская, от 31 мин." -> "от 31 мин."
                if (line2Text != null && metroName != null) {
                    int idx = line2Text.indexOf(metroName);
                    if (idx != -1) {
                        String rest = line2Text.substring(idx + metroName.length())
                                .replaceFirst("^[,\\s]+", "")
                                .trim();
                        if (!rest.isBlank()) {
                            apt.setMetroDistanceRaw(rest);
                            apt.setMetroMinutes(extractLastNumber(rest));
                        }
                    }
                }
            } else {
                // Метро не указано — вторая строка это название района
                apt.setDistrict(line2Text);
            }
        }
    }

    /**
     * Короткое описание — из meta itemprop="description" (Авито его обрезает).
     * Полное описание — видимый текст в нижнем блоке карточки. У Авито
     * там нет стабильного data-marker, поэтому используем префикс класса
     * "iva-item-bottomBlock" (суффикс — хэш сборки, префикс стабилен) и
     * явно исключаем дату публикации, которая лежит в том же блоке.
     * Если разметка поменяется и селектор перестанет находить текст,
     * остаётся хотя бы короткое description — полный текст просто не
     * заполнится.
     */
    private void parseDescription(Element el, Apartment apt) {
        Element descMeta = el.selectFirst("[itemprop=description]");
        if (descMeta != null) {
            apt.setDescription(cleanText(descMeta.attr("content")));
        }

        Element fullDescEl = el.selectFirst("div[class^=iva-item-bottomBlock] p:not([data-marker=item-date])");
        if (fullDescEl != null) {
            apt.setDescriptionFull(cleanText(fullDescEl.text()));
        }
    }

    /**
     * Собирает ВСЕ фотографии объявления, а не только первую.
     * Поддерживает два формата разметки Авито:
     *   1) Актуальный: URL картинок закодирован в data-marker
     *      слайдов фотослайдера — data-marker="slider-image/image-&lt;URL&gt;".
     *      Тег &lt;img&gt; с src в статичном HTML часто отсутствует
     *      (картинки лениво подгружаются JS).
     *   2) Старый: элементы с itemprop="image" и атрибутом src.
     */
    private void parseImages(Element el, Apartment apt) {
        List<String> urls = new ArrayList<>();

        Elements sliderImages = el.select("[data-marker^=slider-image/image-]");
        String prefix = "slider-image/image-";
        for (Element sliderImg : sliderImages) {
            String marker = sliderImg.attr("data-marker");
            int idx = marker.indexOf(prefix);
            if (idx != -1) {
                String url = marker.substring(idx + prefix.length());
                if (!url.isBlank() && !urls.contains(url)) {
                    urls.add(url);
                }
            }
        }

        if (urls.isEmpty()) {
            Elements imgEls = el.select("[itemprop=image]");
            for (Element imgEl : imgEls) {
                String src = imgEl.attr("src");
                if (!src.isBlank() && !urls.contains(src)) {
                    urls.add(src);
                }
            }
        }

        for (int i = 0; i < urls.size(); i++) {
            apt.addImage(ApartmentImage.builder().url(urls.get(i)).position(i).build());
        }

        apt.setImageUrl(urls.isEmpty() ? null : urls.getFirst());
    }

    private void parseUrl(Element el, Apartment apt) {
        Element urlEl = el.selectFirst("a[itemprop=url]");
        if (urlEl != null) {
            apt.setUrl(absoluteUrl(urlEl.attr("href")));
        }
    }

    /**
     * Плашка "Новое объявление" на фото и признак платного продвижения
     * (VAS). Класс продвижения хэширован ("...vas-icon_type-promoted-uOr_s"),
     * поэтому используется contains-селектор по стабильной части имени.
     */
    private void parseStatusFlags(Element el, Apartment apt) {
        boolean isNew = false;
        for (Element badge : el.select("[class*=textBadgeContent]")) {
            if ("Новое объявление".equalsIgnoreCase(cleanText(badge.text()))) {
                isNew = true;
                break;
            }
        }
        apt.setIsNew(isNew);

        boolean promoted = !el.select("[class*=vas-icon_type-promoted]").isEmpty();
        apt.setIsPromoted(promoted);

        apt.setPublishedDateRaw(text(el, "[data-marker=item-date]"));
    }

    /**
     * Данные продавца: имя/название агентства, ссылка на профиль и
     * строка вида "3 завершённых объявления" (плюс число из неё, если
     * формат строки совпал с ожидаемым).
     */
    private void parseSeller(Element el, Apartment apt) {
        Element sellerBlock = el.selectFirst("[class^=iva-item-sellerInfo]");
        if (sellerBlock == null) return;

        Element nameLink = sellerBlock.selectFirst("a[href^=/user/]");
        if (nameLink != null) {
            apt.setSellerName(cleanText(nameLink.text()));
            apt.setSellerProfileUrl(absoluteUrl(nameLink.attr("href")));
        }

        Element listingsEl = sellerBlock.selectFirst("[class^=iva-item-text]");
        if (listingsEl != null) {
            String raw = cleanText(listingsEl.text());
            apt.setSellerCompletedListingsRaw(raw);
            if (raw != null) {
                Matcher m = DIGITS.matcher(raw);
                if (m.find()) {
                    apt.setSellerCompletedListingsCount(parseIntSafe(m.group()));
                }
            }
        }
    }

    /**
     * Бейджи объявления (data-marker="iva-item/&lt;код&gt;", например
     * "Собственник", "Проверено в Росреестре", "Кирпичный дом") и
     * бейджи продавца (data-marker="badge-title-&lt;код&gt;", например
     * "Документы проверены", "Реквизиты проверены").
     */
    private void parseBadges(Element el, Apartment apt) {
        for (Element badgeEl : el.select("[data-marker^=iva-item/]")) {
            String label = cleanText(badgeEl.text());
            if (label == null || label.isBlank()) continue;
            apt.addBadge(ApartmentBadge.builder()
                    .type(ApartmentBadge.BadgeType.ITEM)
                    .code(badgeEl.attr("data-marker"))
                    .label(label)
                    .build());
        }

        for (Element badgeEl : el.select("[data-marker^=badge-title]")) {
            String label = cleanText(badgeEl.text());
            if (label == null || label.isBlank()) continue;
            apt.addBadge(ApartmentBadge.builder()
                    .type(ApartmentBadge.BadgeType.SELLER)
                    .code(badgeEl.attr("data-marker"))
                    .label(label)
                    .build());
        }
    }

    /** Upsert: обновляет существующую запись (включая фото и бейджи) или создаёт новую. */
    private Apartment upsert(Apartment incoming) {
        return repository.findByAvitoId(incoming.getAvitoId())
                .map(existing -> {
                    existing.setTitle(incoming.getTitle());
                    existing.setRooms(incoming.getRooms());
                    existing.setStudio(incoming.getStudio());
                    existing.setTotalArea(incoming.getTotalArea());
                    existing.setFloor(incoming.getFloor());
                    existing.setTotalFloors(incoming.getTotalFloors());

                    existing.setPrice(incoming.getPrice());
                    existing.setPriceRaw(incoming.getPriceRaw());
                    existing.setCurrency(incoming.getCurrency());
                    existing.setPricePerMeter(incoming.getPricePerMeter());

                    existing.setAddress(incoming.getAddress());
                    existing.setStreet(incoming.getStreet());
                    existing.setStreetLink(incoming.getStreetLink());
                    existing.setHouseNumber(incoming.getHouseNumber());
                    existing.setHouseLink(incoming.getHouseLink());
                    existing.setMetro(incoming.getMetro());
                    existing.setMetroName(incoming.getMetroName());
                    existing.setMetroLink(incoming.getMetroLink());
                    existing.setMetroDistanceRaw(incoming.getMetroDistanceRaw());
                    existing.setMetroMinutes(incoming.getMetroMinutes());
                    existing.setDistrict(incoming.getDistrict());
                    existing.setLatitude(incoming.getLatitude());
                    existing.setLongitude(incoming.getLongitude());

                    existing.setDescription(incoming.getDescription());
                    existing.setDescriptionFull(incoming.getDescriptionFull());

                    existing.setUrl(incoming.getUrl());
                    existing.setImageUrl(incoming.getImageUrl());
                    existing.replaceImages(incoming.getImages());
                    existing.replaceBadges(incoming.getBadges());

                    existing.setIsNew(incoming.getIsNew());
                    existing.setIsPromoted(incoming.getIsPromoted());
                    existing.setPublishedDateRaw(incoming.getPublishedDateRaw());

                    existing.setSellerName(incoming.getSellerName());
                    existing.setSellerProfileUrl(incoming.getSellerProfileUrl());
                    existing.setSellerCompletedListingsRaw(incoming.getSellerCompletedListingsRaw());
                    existing.setSellerCompletedListingsCount(incoming.getSellerCompletedListingsCount());

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

    /** Достраивает относительную ссылку Авито до абсолютной. */
    private String absoluteUrl(String href) {
        if (href == null || href.isBlank()) return null;
        return href.startsWith("http") ? href : AVITO_BASE + href;
    }

    /**
     * Преобразует строку вида "13 146 300 ₽" или "133 581 ₽ за м²" в Long,
     * удаляя все нецифровые символы.
     */
    private Long parseLongDigits(String raw) {
        if (raw == null || raw.isBlank()) return null;
        String digits = raw.replaceAll("[^\\d]", "");
        if (digits.isEmpty()) return null;
        try {
            return Long.parseLong(digits);
        } catch (NumberFormatException e) {
            log.warn("Cannot parse number: {}", raw);
            return null;
        }
    }

    private Integer parseIntSafe(String raw) {
        if (raw == null || raw.isBlank()) return null;
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Double parseDoubleSafe(String raw) {
        if (raw == null || raw.isBlank()) return null;
        try {
            return Double.parseDouble(raw.replace(',', '.'));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Из строки вида "от 31 мин." / "21–30 мин." / "до 5 мин." достаёт
     * последнее (то есть наибольшее, верхнюю границу) число.
     */
    private Integer extractLastNumber(String raw) {
        if (raw == null) return null;
        Matcher m = DIGITS.matcher(raw);
        Integer last = null;
        while (m.find()) {
            last = parseIntSafe(m.group());
        }
        return last;
    }
}