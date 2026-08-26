package my.help.finance.avito.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import my.help.finance.avito.entity.Apartment;
import my.help.finance.avito.entity.ApartmentImage;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class AvitoDetailPageParserService {

    private static final Pattern DIGITS_DECIMAL = Pattern.compile("(\\d+(?:[.,]\\d+)?)");
    private static final Pattern DIGITS = Pattern.compile("\\d+");
    private static final Pattern OWNERS_COUNT = Pattern.compile("\\d+\\s*собственник", Pattern.CASE_INSENSITIVE);
    private static final Pattern LAST_OWNER_CHANGE = Pattern.compile("смена собственника", Pattern.CASE_INSENSITIVE);
    private static final Pattern CADASTRAL_NUMBER_JSON = Pattern.compile("\"cadastralNumber\"\\s*:\\s*\"([^\"]*)\"");

    private final ObjectMapper objectMapper = new ObjectMapper();

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
        parseRosreestrCheck(doc, apt);

        return true;
    }

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

        Element contactsBlock = doc.selectFirst("[data-marker=item-view/item-view-contacts]");
        if (contactsBlock != null) {
            Matcher m = Pattern.compile("([\\d\\s]{3,})\\s*₽\\s*за\\s*м²").matcher(contactsBlock.text());
            if (m.find()) {
                Long perMeter = parseLongDigits(m.group(1));
                if (perMeter != null) apt.setPricePerMeter(perMeter);
            }
        }
    }

    private void parseDescription(Document doc, Apartment apt) {
        Element descEl = doc.selectFirst("[data-marker=item-view/item-description]");
        if (descEl != null) {
            apt.setDescriptionFullDetail(cleanText(descEl.text()));
        }
    }

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

        putArea(all, "Площадь кухни", apt::setKitchenArea);
        putArea(all, "Жилая площадь", apt::setLivingArea);
        putString(all, "Балкон или лоджия", apt::setBalconyOrLoggia);
        putString(all, "Тип комнат", apt::setRoomsType);
        putString(all, "Санузел", apt::setBathroomType);
        putString(all, "Окна", apt::setWindowsView);
        putString(all, "Ремонт", apt::setRenovation);
        putString(all, "Способ продажи", apt::setSaleMethod);
        putString(all, "Условия продажи", apt::setSaleConditions);
        putArea(all, "Высота потолков", apt::setCeilingHeight);
        putString(all, "Стоимость ремонта", apt::setRenovationCostEstimate);

        String floorRaw = all.get("Этаж");
        if (floorRaw != null && apt.getFloor() == null) {
            Matcher m = Pattern.compile("(\\d+)\\s*из\\s*(\\d+)").matcher(floorRaw);
            if (m.find()) {
                apt.setFloor(parseIntSafe(m.group(1)));
                if (apt.getTotalFloors() == null) apt.setTotalFloors(parseIntSafe(m.group(2)));
            }
        }

        putString(all, "Тип дома", apt::setBuildingType);
        putString(all, "Пассажирский лифт", apt::setPassengerElevator);
        putString(all, "Грузовой лифт", apt::setFreightElevator);
        putString(all, "В доме", apt::setHouseUtilities);
        putString(all, "Двор", apt::setYardFeatures);
        putString(all, "Парковка", apt::setParking);

        String yearBuiltRaw = all.get("Год постройки");
        if (yearBuiltRaw != null) {
            apt.setYearBuilt(parseIntSafe(yearBuiltRaw));
        }

        String houseFloors = all.get("Этажей в доме");
        if (houseFloors != null && apt.getTotalFloors() == null) {
            apt.setTotalFloors(parseIntSafe(houseFloors));
        }

        parseHouseRating(doc, apt);
    }

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

    private void parsePhoneAndContact(Document doc, Apartment apt) {
        Elements phoneParagraphs = doc.select("[data-marker=item-phone-button/card] p, [data-marker=item-phone-button/header] p");
        for (Element p : phoneParagraphs) {
            String t = p.text().trim();
            if (t.matches(".*\\d.*[X×xX].*") || t.matches("[\\d\\sXX\\-+]{7,}")) {
                apt.setPhoneMasked(t);
                break;
            }
        }

        Matcher m = Pattern.compile(
                "title=\"([А-ЯЁ][а-яё\\-]+(?:\\s[А-ЯЁ][а-яё\\-]+){0,2})\">\\s*\\1\\s*<"
        ).matcher(doc.outerHtml());
        if (m.find()) {
            apt.setContactPersonName(m.group(1));
        }
    }

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

    private void parsePhotos(Document doc, Apartment apt) {
        Elements imgs = doc.select("[data-marker=image-preview/item] img");
        if (imgs.isEmpty()) return;

        java.util.List<ApartmentImage> newImages = new java.util.ArrayList<>();
        int position = 0;
        for (Element img : imgs) {
            String src = img.hasAttr("src") ? img.attr("src") : img.attr("data-src");
            if (src.isBlank()) continue;
            newImages.add(ApartmentImage.builder().url(src).position(position++).build());
        }

        if (!newImages.isEmpty()) {
            apt.replaceImages(newImages);
            if (apt.getImageUrl() == null) {
                apt.setImageUrl(newImages.getFirst().getUrl());
            }
        }
    }

    private void parseRosreestrCheck(Document doc, Apartment apt) {
        Element block = doc.selectFirst("[data-marker=domoteka-entry-block]");
        if (block == null) {
            return;
        }

        Element titleEl = block.selectFirst("h2");
        if (titleEl != null) {
            apt.setRosreestrCheckTitle(cleanText(titleEl.text()));
        }

        Elements items = block.select("p[data-marker=TeaserData.item]");
        if (items.isEmpty()) {
            return;
        }

        List<String> texts = new ArrayList<>();
        for (Element item : items) {
            String t = cleanText(item.text());
            if (t != null) {
                texts.add(t);
            }
        }
        if (texts.isEmpty()) {
            return;
        }

        try {
            apt.setRosreestrChecksJson(objectMapper.writeValueAsString(texts));
        } catch (Exception e) {
            log.warn("Не удалось сериализовать проверки Росреестра {}: {}", apt.getAvitoId(), e.getMessage());
        }

        for (String t : texts) {
            String lower = t.toLowerCase();

            if (OWNERS_COUNT.matcher(t).find()) {
                apt.setRosreestrOwnersCountRaw(t);
            }
            if (LAST_OWNER_CHANGE.matcher(t).find()) {
                apt.setRosreestrLastOwnerChangeRaw(t);
            }

            if (lower.contains("не найдены ограничения") || lower.contains("не найдены обременения")) {
                apt.setRosreestrHasRestrictions(false);
            } else if (lower.contains("ограничен") || lower.contains("обременен")) {
                apt.setRosreestrHasRestrictions(true);
            }

            if (lower.contains("совпадают площадь") || lower.contains("совпадают адрес")) {
                apt.setRosreestrDataMatches(true);
            } else if (lower.contains("не совпада")) {
                apt.setRosreestrDataMatches(false);
            }
        }

        Matcher cadastralMatcher = CADASTRAL_NUMBER_JSON.matcher(doc.outerHtml());
        if (cadastralMatcher.find()) {
            String cadastral = cadastralMatcher.group(1);
            if (cadastral != null && !cadastral.isBlank()) {
                apt.setRosreestrCadastralNumber(cadastral);
            }
        }
    }

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