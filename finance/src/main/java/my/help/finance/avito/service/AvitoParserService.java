package my.help.finance.avito.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.help.finance.avito.entity.Apartment;
import my.help.finance.avito.repository.ApartmentBadgeRepository;
import my.help.finance.avito.repository.ApartmentImageRepository;
import my.help.finance.avito.repository.ApartmentRepository;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Парсер страницы ПОИСКА Авито.
 *
 * Задача первого сбора — только поставить объявления в очередь на обход:
 * собираем исключительно {@code avitoId} и {@code url}.
 * Всё остальное (цена, адрес, метро, описание, фото, бейджи, продавец,
 * координаты, Росреестр и т.п.) добирает уже бот-обходчик
 * ({@link AvitoVisitorBotService} + {@link AvitoDetailPageParserService})
 * со страницы отдельного объявления.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AvitoParserService {

    private static final String STOP_MARKER = "Вас может заинтересовать";
    private static final String AVITO_BASE  = "https://www.avito.ru";

    private final ApartmentRepository repository;
    private final ApartmentImageRepository imageRepository;
    private final ApartmentBadgeRepository badgeRepository;

    /**
     * Обрабатывает НЕСКОЛЬКО HTML-файлов страницы поиска за одну транзакцию.
     * Дубли (одинаковый avitoId) схлопываются в памяти ДО обращения к БД.
     * Для каждого уникального avitoId:
     *   — если записи нет — создаётся (только avitoId + url);
     *   — если запись уже есть — НЕ перетирается, чтобы не потерять
     *     данные, собранные ботом-обходчиком детальных страниц.
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

    @Transactional
    public void deleteAllApartments() {
        imageRepository.deleteAllInBatch();
        badgeRepository.deleteAllInBatch();
        repository.deleteAllInBatch();
    }

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
    //  Парсинг — только avitoId + url
    // ------------------------------------------------------------------

    private List<Apartment> parseHtml(String html) {
        int stopIdx = html.indexOf(STOP_MARKER);
        String cleanHtml = (stopIdx != -1) ? html.substring(0, stopIdx) : html;

        Document doc = Jsoup.parse(cleanHtml);

        // Игнорируем блок "Квартиры в новых ЖК"
        doc.select("[data-marker=itemsCarousel]").remove();

        Elements items = doc.select("[data-marker=item]");

        List<Apartment> result = new ArrayList<>();
        for (Element el : items) {
            parseItem(el).ifPresent(result::add);
        }
        return result;
    }

    /**
     * Из карточки объявления на странице поиска достаём только:
     *   — data-item-id  → avitoId
     *   — href ссылки    → url
     * Больше ничего не парсим: всё остальное появится после обхода
     * объявления ботом-обходчиком.
     */
    private Optional<Apartment> parseItem(Element el) {
        String avitoId = el.attr("data-item-id");
        if (avitoId.isBlank()) return Optional.empty();

        Element urlEl = el.selectFirst("a[itemprop=url]");
        if (urlEl == null) return Optional.empty();

        String href = urlEl.attr("href");
        if (href.isBlank()) return Optional.empty();

        Apartment apt = new Apartment();
        apt.setAvitoId(avitoId);
        apt.setUrl(absoluteUrl(href));
        return Optional.of(apt);
    }

    // ------------------------------------------------------------------
    //  Upsert — не перетираем уже обогащённые данные
    // ------------------------------------------------------------------

    private Apartment upsert(Apartment incoming) {
        return repository.findByAvitoId(incoming.getAvitoId())
                .map(existing -> {
                    // Первый сбор кладёт только avitoId и url.
                    // Если запись уже есть — не трогаем её: детальные данные
                    // (цена, адрес, фото, Росреестр и т.п.) собирает бот-обходчик.
                    // Дописываем только url, если он вдруг оказался пуст.
                    if (existing.getUrl() == null || existing.getUrl().isBlank()) {
                        existing.setUrl(incoming.getUrl());
                        return repository.save(existing);
                    }
                    return existing;
                })
                .orElseGet(() -> repository.save(incoming));
    }

    // ------------------------------------------------------------------

    private String absoluteUrl(String href) {
        if (href == null || href.isBlank()) return null;
        return href.startsWith("http") ? href : AVITO_BASE + href;
    }
}