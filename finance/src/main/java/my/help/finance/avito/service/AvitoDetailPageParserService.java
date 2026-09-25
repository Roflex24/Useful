package my.help.finance.avito.service;

import lombok.extern.slf4j.Slf4j;
import my.help.finance.avito.entity.Apartment;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Парсер СТРАНИЦЫ ОБЪЯВЛЕНИЯ. Извлекает только поля, оставшиеся
 * в сущности {@link Apartment}.
 */
@Slf4j
@Service
public class AvitoDetailPageParserService {

    private static final Pattern DIGITS_DECIMAL = Pattern.compile("(\\d+(?:[.,]\\d+)?)");
    private static final Pattern DIGITS = Pattern.compile("\\d+");
    private static final Pattern ADDRESS_JSON_PATTERN =
            Pattern.compile("\\\\?\"address\\\\?\"\\s*:\\s*\\\\?\"([^\\\\\"]{10,300})\\\\?\"");

    public boolean enrichFromDetailHtml(Apartment apt, String html) {
        if (html == null || html.length() < 2000) return false;

        Document doc;
        try {
            doc = Jsoup.parse(html);
        } catch (Exception e) {
            log.warn("Не удалось распарсить HTML детальной страницы {}: {}", apt.getAvitoId(), e.getMessage());
            return false;
        }

        boolean looksLikeItemPage = doc.selectFirst("[data-marker=item-view/title-info]") != null
                || doc.selectFirst("[data-marker=item-view/item-params]") != null;
        if (!looksLikeItemPage) return false;

        parseAvitoId(doc, apt);
        parseTitle(doc, apt);
        parsePrice(doc, apt);
        parseParams(doc, apt);
        parseAddressAndCoords(doc, apt);
        parseMetro(doc, apt);
        parsePhoto(doc, apt);

        // Инфраструктурные поля. Если это уже делается в вызывающем сервисе —
        // эти строки можно удалить.
        apt.setDetailVisited(true);
        apt.setDetailVisitedAt(LocalDateTime.now());
        if (apt.getDetailVisitAttempts() == null) {
            apt.setDetailVisitAttempts(1);
        } else {
            apt.setDetailVisitAttempts(apt.getDetailVisitAttempts() + 1);
        }

        return true;
    }

    private void parseAvitoId(Document doc, Apartment apt) {
        if (apt.getAvitoId() != null && !apt.getAvitoId().isBlank()) return;

        Element idEl = doc.selectFirst("[data-marker=item-view/item-id]");
        if (idEl != null) {
            String digits = idEl.text().replaceAll("\\D", "");
            if (!digits.isEmpty()) {
                apt.setAvitoId(digits);
                return;
            }
        }

        // запасной вариант — canonical-ссылка
        Element canonical = doc.selectFirst("link[rel=canonical]");
        if (canonical != null) {
            Matcher m = Pattern.compile("(\\d{6,})").matcher(canonical.attr("href"));
            if (m.find()) {
                apt.setAvitoId(m.group(1));
            }
        }
    }

    private void parseTitle(Document doc, Apartment apt) {
        Element titleEl = doc.selectFirst("[data-marker=item-view/title-info]");
        if (titleEl != null) {
            String t = cleanText(titleEl.text());
            if (t != null) apt.setTitle(t);
        }
    }

    private void parsePrice(Document doc, Apartment apt) {
        Element priceEl = doc.selectFirst("[data-marker=item-view/item-price]");
        if (priceEl != null) {
            // В новой вёрстке цена лежит в тексте, а не в content
            Long price = parseLongDigits(priceEl.text());
            if (price == null) {
                price = parseLongDigits(priceEl.attr("content"));
            }
            if (price != null) apt.setPrice(price);

            String priceText = priceEl.text();
            if (priceText.contains("₽") || priceText.toLowerCase().contains("руб")) {
                apt.setCurrency("RUB");
            }
        }

        // цена за м² — либо из блока контактов, либо посчитать
        Element contactsBlock = doc.selectFirst("[data-marker=item-view/item-view-contacts]");
        if (contactsBlock != null) {
            Matcher m = Pattern.compile("([\\d\\s\\u00A0]{3,})\\s*₽\\s*за\\s*м²")
                    .matcher(contactsBlock.text());
            if (m.find()) {
                Long perMeter = parseLongDigits(m.group(1));
                if (perMeter != null) apt.setPricePerMeter(perMeter);
            }
        }
        if (apt.getPricePerMeter() == null
                && apt.getPrice() != null && apt.getTotalArea() != null && apt.getTotalArea() > 0) {
            apt.setPricePerMeter(Math.round(apt.getPrice() / apt.getTotalArea()));
        }
    }

    private void parseParams(Document doc, Apartment apt) {
        Map<String, String> all = new LinkedHashMap<>();
        Elements blocks = doc.select("[data-marker=item-view/item-params]");
        for (Element block : blocks) {
            all.putAll(parseParamsList(block));
        }
        if (all.isEmpty()) return;

        // ── Комнаты / студия
        String roomsRaw = all.get("Количество комнат");
        if (roomsRaw != null) {
            Integer rooms = parseIntSafe(roomsRaw);
            if (rooms != null) apt.setRooms(rooms);
        }

        // ── Общая площадь
        putArea(all, "Общая площадь", apt::setTotalArea);

        // ── Этаж
        String floorRaw = all.get("Этаж");
        if (floorRaw != null) {
            Matcher m = Pattern.compile("(\\d+)\\s*из\\s*(\\d+)").matcher(floorRaw);
            if (m.find()) {
                apt.setFloor(parseIntSafe(m.group(1)));
                apt.setTotalFloors(parseIntSafe(m.group(2)));
            } else {
                Integer f = parseIntSafe(floorRaw);
                if (f != null) apt.setFloor(f);
            }
        }

        // ── Дом
        putString(all, "Тип дома", apt::setBuildingType);
        putString(all, "Ремонт", apt::setRenovation);

        String yearBuiltRaw = all.get("Год постройки");
        if (yearBuiltRaw != null) apt.setYearBuilt(parseIntSafe(yearBuiltRaw));
    }

    private void parseAddressAndCoords(Document doc, Apartment apt) {
        String address = null;

        Element addressBlock = doc.selectFirst("#item-view-address");
        if (addressBlock == null) {
            log.warn("#item-view-address не найден для {}", apt.getAvitoId());
        } else {
            address = extractAddressFromBlock(addressBlock);
        }

        // запасной вариант — вытащить из JSON, встроенного в страницу
        if (address == null) {
            address = extractAddressFromJson(doc.html());
        }

        if (address != null) {
            apt.setAddress(address);
            log.debug("Адрес для {}: {}", apt.getAvitoId(), address);
        } else {
            log.warn("Не удалось извлечь адрес для {}", apt.getAvitoId());
        }

        Element mapWrapper = doc.selectFirst("[data-marker='item-map-wrapper']");
        if (mapWrapper != null) {
            Double lat = parseDoubleSafe(mapWrapper.attr("data-map-lat"));
            Double lon = parseDoubleSafe(mapWrapper.attr("data-map-lon"));
            if (lat != null) apt.setLatitude(lat);
            if (lon != null) apt.setLongitude(lon);
        }
    }

    private String extractAddressFromBlock(Element block) {
        // 1) Точный класс из текущей вёрстки Авито
        Element span = block.selectFirst("span._8360df6eedcf8d52");
        if (span != null) {
            String t = cleanText(span.text());
            if (t != null && !t.isBlank()) return t;
        }

        // 2) itemPro poperty (старая вёрстка)
        Element ip = block.selectFirst("[itemprop=address]");
        if (ip != null) {
            String t = cleanText(ip.text());
            if (t != null && !t.isBlank()) return t;
        }

        // 3) Первый LEAF-span, похожий на адрес (нет вложенных span)
        for (Element el : block.select("span")) {
            if (!el.select("> span").isEmpty()) continue;
            String t = cleanText(el.text());
            if (t == null) continue;
            if (t.length() < 10 || t.length() > 300) continue;
            if (t.contains("мин") || t.contains("метро") || t.contains("карт")) continue;
            if (!t.contains(",")) continue;
            return t;
        }

        // 4) Любой leaf-span с буквами и цифрами
        for (Element el : block.select("span")) {
            if (!el.select("> span").isEmpty()) continue;
            String t = cleanText(el.text());
            if (t == null) continue;
            if (t.length() < 15) continue;
            if (t.contains("мин") || t.contains("метро") || t.contains("карт")) continue;
            if (!t.matches(".*[А-Яа-яЁё].*\\d.*")) continue;
            return t;
        }

        return null;
    }

    private String extractAddressFromJson(String html) {
        if (html == null || html.isEmpty()) return null;
        Matcher m = ADDRESS_JSON_PATTERN.matcher(html);
        if (m.find()) {
            String addr = m.group(1);
            if (addr != null) {
                addr = addr.replace("\\/", "/").trim();
                if (!addr.isEmpty()) return addr;
            }
        }
        return null;
    }

    private void parseMetro(Document doc, Apartment apt) {
        Element addressBlock = doc.selectFirst("#item-view-address");
        if (addressBlock == null) return;

        // В новой вёрстке метро лежит внутри #item-view-address
        // Пример текста: "Канавинская11–15 мин."
        for (Element span : addressBlock.select("span")) {
            String text = cleanText(span.text());
            if (text == null || !text.contains("мин")) continue;

            Matcher m = Pattern.compile("^(.*?)\\s*(\\d+).*?мин").matcher(text);
            if (m.find()) {
                String name = m.group(1).trim();
                Integer minutes = parseIntSafe(m.group(2));
                if (!name.isBlank()) {
                    apt.setMetroName(name);
                    apt.setMetroMinutes(minutes);
                    return; // берём первую станцию
                }
            }
        }

        // запасной вариант — боковой блок контактов
        Element sideMetro = doc.selectFirst("[data-marker=item-view/item-view-contacts] ._0af34493c0881b75 p");
        if (sideMetro != null) {
            String text = cleanText(sideMetro.text());
            if (text != null) {
                Matcher m = Pattern.compile("^(.*?)\\s*(\\d+).*?мин").matcher(text);
                if (m.find()) {
                    apt.setMetroName(m.group(1).trim());
                    apt.setMetroMinutes(parseIntSafe(m.group(2)));
                }
            }
        }
    }

    private void parsePhoto(Document doc, Apartment apt) {
        // Сначала пробуем главный слайдер, потом всю галерею
        Element img = doc.selectFirst("#gallery-slider img");
        if (img == null) {
            img = doc.selectFirst("[data-marker=item-view/main-gallery] img");
        }
        if (img == null) return;

        String src = img.hasAttr("src") ? img.attr("src") : img.attr("data-src");
        if (src != null && !src.isBlank()) {
            apt.setImageUrl(src);
        }
    }

    // ── helpers ─────────────────────────────────────────────

    private Map<String, String> parseParamsList(Element paramsBlock) {
        Map<String, String> map = new LinkedHashMap<>();
        for (Element li : paramsBlock.select("ul > li")) {
            Element labelSpan = li.selectFirst("span");
            if (labelSpan == null) continue;

            String label = labelSpan.text().replaceAll("[:\\s]+$", "").trim();
            if (label.isEmpty()) continue;

            String value = cleanText(li.ownText());
            if (value == null || value.isEmpty()) {
                String full = li.text();
                String withoutLabel = full.replaceFirst(Pattern.quote(labelSpan.text()), "").trim();
                value = cleanText(withoutLabel);
            }
            if (value != null && !value.isEmpty()) map.put(label, value);
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

    private String cleanText(String raw) {
        if (raw == null) return null;
        String t = raw.replace("\u00A0", " ").trim();
        return t.isEmpty() ? null : t;
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