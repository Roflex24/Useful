package my.help.finance.avito;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * REST-контроллер для загрузки HTML и получения квартир.
 *
 * Эндпоинты:
 *   POST /api/apartments/parse  — загрузить один или несколько HTML-файлов,
 *                                 распарсить и сохранить (обновить существующие)
 *   GET  /api/apartments        — получить все квартиры из БД
 *   GET  /api/apartments/{id}   — получить квартиру по avito_id
 *   DELETE /api/apartments      — полностью очистить базу
 */
@RestController
@RequestMapping("/api/apartments")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ApartmentController {

    private final AvitoParserService parserService;
    private final ApartmentRepository repository;

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
    // DTO ответа
    // ------------------------------------------------------------------

    public record ParseResponse(
            int parsedCount,
            List<Apartment> apartments,
            String error
    ) {}
}