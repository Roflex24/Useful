package my.help.finance.avito;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * REST-контроллер для загрузки HTML и получения квартир.
 *
 * Эндпоинты:
 *   POST /api/apartments/parse          — загрузить один или несколько HTML-файлов,
 *                                         распарсить и сохранить (обновить существующие)
 *   GET  /api/apartments                — получить все квартиры из БД
 *   GET  /api/apartments/{id}           — получить квартиру по avito_id
 *   DELETE /api/apartments              — полностью очистить базу
 *   POST /api/apartments/bot/start      — запустить бота-обходчика (java.awt.Robot)
 *   POST /api/apartments/bot/stop       — остановить бота после текущего шага
 *   GET  /api/apartments/bot/status     — прогресс бота (для опроса с фронтенда)
 *   GET  /api/apartments/bot/queue      — объявления, страницы которых бот ещё не скачал
 *   POST /api/apartments/{avitoId}/bot/visited — отметить объявление как посещённое
 *                                                (используется внешним standalone-ботом)
 */
@RestController
@RequestMapping("/api/apartments")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ApartmentController {

    private final AvitoParserService parserService;
    private final ApartmentRepository repository;
    private final AvitoVisitorBotService botService;
    private final AvitoDetailPageParserService detailParserService;

    // ------------------------------------------------------------------
    // POST /api/apartments/parse
    // ------------------------------------------------------------------

    /**
     * Принимает один или несколько HTML-файлов страниц Авито,
     * парсит объявления и сохраняет/обновляет их в базе данных.
     * Дубли (по avito_id) не создаются — существующая запись обновляется.
     *
     * Пример запроса (curl) с несколькими файлами:
     *   curl -F "files=@page1.html" -F "files=@page2.html" \
     *        http://localhost:8080/api/apartments/parse
     */
    @PostMapping(value = "/parse", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ParseResponse> parseHtmlFiles(
            @RequestParam("files") List<MultipartFile> files
    ) {
        if (files == null || files.isEmpty() || files.stream().allMatch(MultipartFile::isEmpty)) {
            return ResponseEntity.badRequest()
                    .body(new ParseResponse(0, List.of(), "Файлы не выбраны"));
        }

        List<String> htmlContents = new ArrayList<>();
        List<String> skippedFiles = new ArrayList<>();

        for (MultipartFile file : files) {
            if (file.isEmpty()) continue;

            String filename = file.getOriginalFilename();
            if (filename == null || (!filename.endsWith(".html") && !filename.endsWith(".htm"))) {
                skippedFiles.add(filename);
                continue;
            }

            try {
                byte[] bytes = file.getBytes();
                String html;
                try {
                    html = new String(bytes, StandardCharsets.UTF_8);
                } catch (Exception e) {
                    html = new String(bytes, Charset.forName("windows-1251"));
                }
                htmlContents.add(html);
            } catch (IOException e) {
                return ResponseEntity.internalServerError()
                        .body(new ParseResponse(0, List.of(),
                                "Ошибка чтения файла " + filename + ": " + e.getMessage()));
            }
        }

        if (htmlContents.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new ParseResponse(0, List.of(),
                            "Среди загруженных файлов нет подходящих .html/.htm"));
        }

        try {
            List<Apartment> saved = parserService.parseAndSaveMultiple(htmlContents);

            String warning = skippedFiles.isEmpty() ? null
                    : "Пропущены файлы (не .html/.htm): " + skippedFiles;

            return ResponseEntity.ok(new ParseResponse(saved.size(), saved, warning));

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new ParseResponse(0, List.of(), "Ошибка парсинга: " + e.getMessage()));
        }
    }

    // ------------------------------------------------------------------
    // GET /api/apartments
    // ------------------------------------------------------------------

    @GetMapping
    public ResponseEntity<List<Apartment>> getAllApartments() {
        return ResponseEntity.ok(repository.findAll());
    }

    // ------------------------------------------------------------------
    // GET /api/apartments/{avitoId}
    // ------------------------------------------------------------------

    @GetMapping("/{avitoId}")
    public ResponseEntity<Apartment> getByAvitoId(@PathVariable String avitoId) {
        return repository.findByAvitoId(avitoId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ------------------------------------------------------------------
    // DELETE /api/apartments
    // ------------------------------------------------------------------

    /**
     * Полностью очищает таблицу квартир (и связанные фото/бейджи).
     * Используется кнопкой "Очистить базу" на фронтенде.
     *
     * Раньше здесь был repository.deleteAllInBatch() напрямую — это
     * ломалось, как только появились apartment_images/apartment_badges
     * с внешним ключом на apartments: прямой batch-delete родителя не
     * запускает JPA cascade, и БД отвергала запрос из-за нарушения FK.
     * parserService.deleteAllApartments() удаляет в правильном порядке
     * (дети -> родитель).
     */
    @DeleteMapping
    public ResponseEntity<Void> deleteAllApartments() {
        parserService.deleteAllApartments();
        return ResponseEntity.noContent().build();
    }

    // ------------------------------------------------------------------
    // POST /api/apartments/bot/start · POST /bot/stop · GET /bot/status
    // ------------------------------------------------------------------

    /**
     * Запускает бота-обходчика прямо в этом же backend-процессе (кнопка
     * "Запустить бота" на фронтенде). Реальная автоматизация (java.awt.Robot)
     * идёт в фоновом потоке — см. {@link AvitoVisitorBotService}.
     */
    @PostMapping("/bot/start")
    public ResponseEntity<AvitoVisitorBotService.BotStatusDto> startBot() {
        return ResponseEntity.ok(botService.start());
    }

    /** Просит бота остановиться после текущего шага (кнопка "Остановить"). */
    @PostMapping("/bot/stop")
    public ResponseEntity<AvitoVisitorBotService.BotStatusDto> stopBot() {
        return ResponseEntity.ok(botService.stop());
    }

    /** Текущий прогресс — фронтенд опрашивает это раз в пару секунд, пока бот работает. */
    @GetMapping("/bot/status")
    public ResponseEntity<AvitoVisitorBotService.BotStatusDto> botStatus() {
        return ResponseEntity.ok(botService.getStatus());
    }

    // ------------------------------------------------------------------
    // GET /api/apartments/bot/queue
    // ------------------------------------------------------------------

    /**
     * Отдаёт боту-обходчику список объявлений, страницы которых ещё
     * не скачаны (detail_visited не true). Намеренно возвращает только
     * то, что нужно для навигации (id, avitoId, url) — без фото/бейджей,
     * чтобы ответ был лёгким даже при большой очереди.
     */
    @GetMapping("/bot/queue")
    public ResponseEntity<List<BotQueueItem>> getBotQueue() {
        List<BotQueueItem> queue = repository.findQueueForBot().stream()
                .map(a -> new BotQueueItem(a.getId(), a.getAvitoId(), a.getUrl(), a.getDetailVisitAttempts()))
                .toList();
        return ResponseEntity.ok(queue);
    }

    // ------------------------------------------------------------------
    // POST /api/apartments/{avitoId}/bot/visited
    // ------------------------------------------------------------------

    /**
     * Внешний standalone-бот ({@link AvitoVisitorBot}) вызывает это после
     * того, как реально открыл объявление в браузере и скопировал исходник
     * страницы через буфер обмена. Если html передан и похож на страницу
     * объявления — сразу парсим его в поля квартиры (тем же
     * {@link AvitoDetailPageParserService}, что использует и встроенный
     * бот) и помечаем объявление посещённым. Если нет — просто считаем
     * попытку неудачной, объявление останется в очереди.
     */
    @PostMapping("/{avitoId}/bot/visited")
    public ResponseEntity<Apartment> markVisited(
            @PathVariable String avitoId,
            @RequestBody BotVisitedRequest body
    ) {
        return repository.findByAvitoId(avitoId)
                .map(apt -> {
                    int attempts = apt.getDetailVisitAttempts() == null ? 0 : apt.getDetailVisitAttempts();
                    apt.setDetailVisitAttempts(attempts + 1);

                    boolean success = false;
                    if (body != null && body.success() && body.html() != null && !body.html().isBlank()) {
                        success = detailParserService.enrichFromDetailHtml(apt, body.html());
                    }

                    if (success) {
                        apt.setDetailVisited(true);
                        apt.setDetailVisitedAt(LocalDateTime.now());
                    }

                    return ResponseEntity.ok(repository.save(apt));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // ------------------------------------------------------------------
    // DTO
    // ------------------------------------------------------------------

    public record ParseResponse(
            int parsedCount,
            List<Apartment> apartments,
            String error
    ) {}

    /** Один пункт очереди для бота-обходчика. */
    public record BotQueueItem(
            Long id,
            String avitoId,
            String url,
            Integer previousAttempts
    ) {}

    /** Тело запроса, которым внешний бот отчитывается о результате визита. */
    public record BotVisitedRequest(
            boolean success,
            String html
    ) {}
}