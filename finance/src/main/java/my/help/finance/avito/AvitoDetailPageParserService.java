package my.help.finance.avito;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Парсит HTML страницы ОДНОГО объявления Авито (не выдачи поиска) и
 * дополняет уже существующую в базе {@link Apartment} всем, что удаётся
 * из неё вытащить: точные площади, характеристики дома, полный адрес,
 * координаты, счётчики просмотров, замаскированный телефон и т.д.
 *
 * В отличие от {@link AvitoParserService} (парсит карточки в выдаче
 * поиска — там разметка компактная и стабильная), детальная страница
 * собрана из мелких переиспользуемых React-компонентов с рандомными
 * CSS-классами, которые меняются от сборки к сборке. Поэтому здесь,
 * где только можно, парсинг опирается на:
 *   - {@code data-marker="..."} атрибуты — Авито сама использует их
 *     как стабильные хуки для своей аналитики/тестов, не трогает между
 *     деплоями;
 *   - {@code id="..."} и {@code itemprop="..."} — тоже стабильные хуки;
 *   - структуру тегов (например, "второй <p> внутри кнопки телефона");
 * а НЕ на конкретные хэш-классы вида {@code _6a7423df6c704813}, которые
 * гарантированно рассыпятся при следующем обновлении Авито.
 *
 * Раздел "О квартире" и "О доме" устроен как единый плоский список
 * {@code <li><span>Метка: </span>Значение</li>} — вместо того, чтобы
 * перечислять каждую метку отдельным селектором, парсер вычитывает ВСЕ
 * пары ключ-значение в {@link Map} (детально: {@link #parseParamsList}),
 * кладёт их целиком в {@code detailParamsJson} (полный сырой снимок —
 * ничего не теряется, даже если Авито завтра добавит новую характеристику),
 * а затем раскладывает известные на сегодня метки по удобным колонкам.
 */
@Slf4j
@Service
public class AvitoDetailPageParserService {

    private static final Pattern DIGITS_DECIMAL = Pattern.compile("(\\d+(?:[.,]\\d+)?)");
    private static final Pattern DIGITS = Pattern.compile("\\d+");

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Разбирает HTML детальной страницы и заполняет поля переданной
     * квартиры. Ничего не сохраняет в БД — это решает вызывающий код.
     *
     * @return true, если html действительно похож на страницу объявления
     *         Авито и что-то удалось распарсить; false — если страница
     *         не та (например, скопировался не тот текст из буфера обмена,
     *         показалась капча и т.п.) — в этом случае объявление стоит
     *         оставить в очереди для повторной попытки.
     */
    public boolean enrichFromDetailHtml(Apartment apt, String html) {
        if (html == null || html.length() < 2000) {
            return false;
        }

        Document doc;
        try {
            doc = Jsoup.parse(html);
        } catch (Exception e) {
            log.warn("Не удалось распарсить HTML детальной страницы {}: {}", apt.getAvitoId(), e.getMessage());
            return false;
        }

        boolean looksLikeItemPage = doc.selectFirst("[data-marker=item-view/title-info]") != null
                || doc.selectFirst("[data-marker=item-view/item-params]") != null;
        if (!looksLikeItemPage) {
            return false;
        }

        parsePrice(doc, apt);
        parseDescription(doc, apt);
        parseParams(doc, apt);
        parseAddressAndCoords(doc, apt);
        parseViewsAndDate(doc, apt);
        parsePhoneAndContact(doc, apt);
        parseQuickFeatures(doc, apt);
        parsePhotos(doc, apt);

        return true;
    }

    // ------------------------------------------------------------------
    // Цена и цена за метр
    // ------------------------------------------------------------------

    private void parsePrice(Document doc, Apartment apt) {
        Element priceEl = doc.selectFirst("[data-marker=item-view/item-price]");
        if (priceEl != null) {
            Long price = parseLongDigits(priceEl.attr("content"));
            if (price != null) apt.setPrice(price);
            apt.setPriceRaw(cleanText(priceEl.ownText()));
        }

        Element currencyEl = doc.selectFirst("[data-marker=item-view/item-price] [itemprop=priceCurrency]");
        if (currencyEl != null && !currencyEl.attr("content").isBlank()) {
            apt.setCurrency(currencyEl.attr("content"));
        }

        // "119 048 ₽ за м²" — обычный текстовый блок рядом с ценой, без стабильного маркера,
        // поэтому ищем по шаблону "число + ₽ за м²" во всём тексте контейнера контактов.
        Element contactsBlock = doc.selectFirst("[data-marker=item-view/item-view-contacts]");
        if (contactsBlock != null) {
            Matcher m = Pattern.compile("([\\d\\s]{3,})\\s*₽\\s*за\\s*м²").matcher(contactsBlock.text());
            if (m.find()) {
                Long perMeter = parseLongDigits(m.group(1));
                if (perMeter != null) apt.setPricePerMeter(perMeter);
            }
        }
    }

    // ------------------------------------------------------------------
    // Описание
    // ------------------------------------------------------------------

    private void parseDescription(Document doc, Apartment apt) {
        Element descEl = doc.selectFirst("[data-marker=item-view/item-description]");
        if (descEl != null) {
            apt.setDescriptionFullDetail(cleanText(descEl.text()));
        }
    }

    // ------------------------------------------------------------------
    // "О квартире" + "О доме" — общий плоский список характеристик
    // ------------------------------------------------------------------

    private void parseParams(Document doc, Apartment apt) {
        Map<String, String> all = new LinkedHashMap<>();
        Elements blocks = doc.select("[data-marker=item-view/item-params]");
        for (Element block : blocks) {
            all.putAll(parseParamsList(block));
        }

        if (all.isEmpty()) return;

        try {
            apt.setDetailParamsJson(objectMapper.writeValueAsString(all));
        } catch (Exception e) {
            log.warn("Не удалось сериализовать характеристики {} в JSON: {}", apt.getAvitoId(), e.getMessage());
        }

        // "О квартире"
        putArea(all, "Площадь кухни", apt::setKitchenArea);
        putArea(all, "Жилая площадь", apt::setLivingArea);
        putString(all, "Балкон или лоджия", apt::setBalconyOrLoggia);
        putString(all, "Тип комнат", apt::setRoomsType);
        putString(all, "Санузел", apt::setBathroomType);
        putString(all, "Окна", apt::setWindowsView);
        putString(all, "Ремонт", apt::setRenovation);
        putString(all, "Способ продажи", apt::setSaleMethod);
        putString(all, "Условия продажи", apt::setSaleConditions);

        // "Этаж" здесь дублирует заголовок ("5 из 9") — используем только
        // как подстраховку, если по какой-то причине не распарсился заголовок.
        String floorRaw = all.get("Этаж");
        if (floorRaw != null && apt.getFloor() == null) {
            Matcher m = Pattern.compile("(\\d+)\\s*из\\s*(\\d+)").matcher(floorRaw);
            if (m.find()) {
                apt.setFloor(parseIntSafe(m.group(1)));
                if (apt.getTotalFloors() == null) apt.setTotalFloors(parseIntSafe(m.group(2)));
            }
        }

        // "О доме"
        putString(all, "Тип дома", apt::setBuildingType);
        putString(all, "Пассажирский лифт", apt::setPassengerElevator);
        putString(all, "Грузовой лифт", apt::setFreightElevator);

        String houseFloors = all.get("Этажей в доме");
        if (houseFloors != null && apt.getTotalFloors() == null) {
            apt.setTotalFloors(parseIntSafe(houseFloors));
        }

        parseHouseRating(doc, apt);
    }

    /** Рейтинг дома и число отзывов — стабильный маркер data-marker="rating-and-reviews". */
    private void parseHouseRating(Document doc, Apartment apt) {
        Element ratingBlock = doc.selectFirst("[data-marker=rating-and-reviews]");
        if (ratingBlock == null) return;

        Elements spans = ratingBlock.select("span");
        for (Element span : spans) {
            String t = span.text().trim();
            if (t.matches("\\d[.,]\\d")) {
                apt.setHouseRating(parseDoubleSafe(t));
            } else if (t.toLowerCase().contains("отзыв")) {
                Matcher m = DIGITS.matcher(t);
                if (m.find()) apt.setHouseReviewsCount(parseIntSafe(m.group()));
            }
        }

        Element catalogLink = doc.selectFirst("a[data-marker=nd-jk-details-button]");
        if (catalogLink == null) {
            catalogLink = doc.selectFirst("a[href*=/catalog/houses/]");
        }
        if (catalogLink != null) {
            apt.setHouseCatalogUrl(absoluteUrl(catalogLink.attr("href")));
        }
    }

    /**
     * Общий разбор блока характеристик вида
     * {@code <ul><li><span>Метка<span>: </span></span>Значение</li>...</ul>}
     * в Map "метка -> значение". Работает и для "О квартире", и для "О доме" —
     * это один и тот же паттерн разметки.
     */
    private Map<String, String> parseParamsList(Element paramsBlock) {
        Map<String, String> map = new LinkedHashMap<>();
        for (Element li : paramsBlock.select("ul > li")) {
            Element labelSpan = li.selectFirst("span");
            if (labelSpan == null) continue;

            String label = labelSpan.text().replaceAll("[:\\s]+$", "").trim();
            if (label.isEmpty()) continue;

            String value = cleanText(li.ownText());
            if (value == null || value.isEmpty()) {
                // Значение иногда лежит не прямым текстом <li>, а во вложенном
                // <span> (например, ссылка "Стоимость ремонта") — тогда просто
                // берём весь текст строки за вычетом текста метки.
                String full = li.text();
                String withoutLabel = full.replaceFirst(Pattern.quote(labelSpan.text()), "").trim();
                value = cleanText(withoutLabel);
            }

            if (value != null && !value.isEmpty()) {
                map.put(label, value);
            }
        }
        return map;
    }

    private void putString(Map<String, String> params, String label, java.util.function.Consumer<String> setter) {
        String v = params.get(label);
        if (v != null && !v.isBlank()) setter.accept(v);
    }

    private void putArea(Map<String, String> params, String label, java.util.function.Consumer<Double> setter) {
        String v = params.get(label);
        if (v == null) return;
        Matcher m = DIGITS_DECIMAL.matcher(v);
        if (m.find()) setter.accept(parseDoubleSafe(m.group(1)));
    }

    // ------------------------------------------------------------------
    // Адрес и координаты
    // ------------------------------------------------------------------

    private void parseAddressAndCoords(Document doc, Apartment apt) {
        Element addressBlock = doc.selectFirst("#item-view-address");
        if (addressBlock != null) {
            Element addressSpan = addressBlock.selectFirst("[itemprop=address] span");
            if (addressSpan != null) {
                apt.setFullAddress(cleanText(addressSpan.text()));
            }
            apt.setLocationSectionRaw(cleanText(addressBlock.text()));
        }

        Element mapWrapper = doc.selectFirst("[data-marker=item-map-wrapper]");
        if (mapWrapper != null) {
            Double lat = parseDoubleSafe(mapWrapper.attr("data-map-lat"));
            Double lon = parseDoubleSafe(mapWrapper.attr("data-map-lon"));
            if (lat != null) apt.setLatitude(lat);
            if (lon != null) apt.setLongitude(lon);
        }
    }

    // ------------------------------------------------------------------
    // Просмотры и дата публикации
    // ------------------------------------------------------------------

    private void parseViewsAndDate(Document doc, Apartment apt) {
        Element totalViewsEl = doc.selectFirst("[data-marker=item-view/total-views]");
        if (totalViewsEl != null) {
            Matcher m = DIGITS.matcher(totalViewsEl.text());
            if (m.find()) apt.setTotalViews(parseIntSafe(m.group()));
        }

        Element todayViewsEl = doc.selectFirst("[data-marker=item-view/today-views]");
        if (todayViewsEl != null) {
            Matcher m = DIGITS.matcher(todayViewsEl.text());
            if (m.find()) apt.setTodayViews(parseIntSafe(m.group()));
        }

        Element dateEl = doc.selectFirst("[data-marker=item-view/item-date]");
        if (dateEl != null) {
            String raw = dateEl.text().replaceFirst("^[·\\s]+", "").trim();
            apt.setDetailPublishedRaw(cleanText(raw));
        }
    }

    // ------------------------------------------------------------------
    // Телефон и контактное лицо
    // ------------------------------------------------------------------

    /**
     * Внутри кнопки "Показать телефон" два &lt;p&gt;: первый — подпись
     * кнопки, второй — уже показанный замаскированный номер вида
     * "8 958 XXX-XX-XX". Полный номер без клика недоступен (открывается
     * доп. запросом к API Авито) — сознательно его не добываем.
     */
    private void parsePhoneAndContact(Document doc, Apartment apt) {
        Elements phoneParagraphs = doc.select("[data-marker=item-phone-button/card] p, [data-marker=item-phone-button/header] p");
        for (Element p : phoneParagraphs) {
            String t = p.text().trim();
            if (t.matches(".*\\d.*[X×xX].*") || t.matches("[\\d\\sXX\\-+]{7,}")) {
                apt.setPhoneMasked(t);
                break;
            }
        }

        // Имя контактного лица не имеет стабильного маркера — эвристика:
        // ищем "title="Имя Отчество">Имя Отчество<" (Авито дублирует имя
        // в title того же элемента, что и в его тексте).
        Matcher m = Pattern.compile(
                "title=\"([А-ЯЁ][а-яё\\-]+(?:\\s[А-ЯЁ][а-яё\\-]+){0,2})\">\\s*\\1\\s*<"
        ).matcher(doc.outerHtml());
        if (m.find()) {
            apt.setContactPersonName(m.group(1));
        }
    }

    // ------------------------------------------------------------------
    // Быстрые чипсы-особенности (realty-usp)
    // ------------------------------------------------------------------

    private void parseQuickFeatures(Document doc, Apartment apt) {
        Elements chips = doc.select("[data-marker^=realty-usp/desktop-chips/option]");
        if (chips.isEmpty()) return;

        Pattern optionPattern = Pattern.compile("option\\(([^)]+)\\)");
        var features = new LinkedHashSet<String>();

        for (Element chip : chips) {
            Matcher m = optionPattern.matcher(chip.attr("data-marker"));
            if (!m.find()) continue;
            String raw = m.group(1); // например "Изолир. комнаты-cards"
            int lastDash = raw.lastIndexOf('-');
            String label = lastDash > 0 ? raw.substring(0, lastDash) : raw;
            if (!label.isBlank()) features.add(label.trim());
        }

        if (!features.isEmpty()) {
            apt.setQuickFeatures(String.join(", ", features));
        }
    }

    // ------------------------------------------------------------------
    // Фотографии (заменяют те, что были получены со страницы поиска —
    // на детальной странице их обычно больше и они выше качеством)
    // ------------------------------------------------------------------

    private void parsePhotos(Document doc, Apartment apt) {
        Elements imgs = doc.select("[data-marker=image-preview/item] img");
        if (imgs.isEmpty()) return;

        java.util.List<ApartmentImage> newImages = new java.util.ArrayList<>();
        int position = 0;
        for (Element img : imgs) {
            String src = img.hasAttr("src") ? img.attr("src") : img.attr("data-src");
            if (src == null || src.isBlank()) continue;
            newImages.add(ApartmentImage.builder().url(src).position(position++).build());
        }

        if (!newImages.isEmpty()) {
            apt.replaceImages(newImages);
            if (apt.getImageUrl() == null) {
                apt.setImageUrl(newImages.get(0).getUrl());
            }
        }
    }

    // ------------------------------------------------------------------
    // Мелкие утилиты
    // ------------------------------------------------------------------

    private String cleanText(String raw) {
        if (raw == null) return null;
        String t = raw.replace("\u00A0", " ").trim();
        return t.isEmpty() ? null : t;
    }

    private String absoluteUrl(String href) {
        if (href == null || href.isBlank()) return null;
        if (href.startsWith("http")) return href;
        return "https://www.avito.ru" + href;
    }

    private Integer parseIntSafe(String raw) {
        if (raw == null || raw.isBlank()) return null;
        try {
            return Integer.parseInt(raw.replaceAll("[^\\d]", ""));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Double parseDoubleSafe(String raw) {
        if (raw == null || raw.isBlank()) return null;
        try {
            return Double.parseDouble(raw.trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Long parseLongDigits(String raw) {
        if (raw == null || raw.isBlank()) return null;
        String digits = raw.replaceAll("[^\\d]", "");
        if (digits.isEmpty()) return null;
        try {
            return Long.parseLong(digits);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
