package my.help.finance.avito.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import my.help.finance.avito.dto.*;
import my.help.finance.avito.entity.Apartment;
import my.help.finance.avito.repository.ApartmentRepository;
import my.help.finance.avito.service.*;
import org.springframework.http.HttpStatus;
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

@RestController
@RequestMapping("/api/apartments")
@RequiredArgsConstructor
@Tag(name = "Avito API", description = "Парсинг и работа с авито")
public class ApartmentController {

    private final AvitoParserService parserService;
    private final ApartmentRepository repository;
    private final AvitoVisitorBotService botService;
    private final AvitoDetailPageParserService detailParserService;

    private final ApartmentScoringService scoringService;

    @PostMapping(value = "/parse", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ParseRs> parseHtmlFiles(
            @RequestParam("files") List<MultipartFile> files
    ) {
        if (files == null || files.isEmpty() || files.stream().allMatch(MultipartFile::isEmpty)) {
            return ResponseEntity.badRequest()
                    .body(new ParseRs(0, List.of(), "Файлы не выбраны"));
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
                        .body(new ParseRs(0, List.of(),
                                "Ошибка чтения файла " + filename + ": " + e.getMessage()));
            }
        }

        if (htmlContents.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new ParseRs(0, List.of(),
                            "Среди загруженных файлов нет подходящих .html/.htm"));
        }

        try {
            List<Apartment> saved = parserService.parseAndSaveMultiple(htmlContents);

            String warning = skippedFiles.isEmpty() ? null
                    : "Пропущены файлы (не .html/.htm): " + skippedFiles;

            return ResponseEntity.ok(new ParseRs(saved.size(), saved, warning));

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new ParseRs(0, List.of(), "Ошибка парсинга: " + e.getMessage()));
        }
    }

    @GetMapping
    public List<Apartment> getAllApartments() {
        return repository.findAll();
    }

    @GetMapping("/{avitoId}")
    public ResponseEntity<Apartment> getByAvitoId(@PathVariable String avitoId) {
        return repository.findByAvitoId(avitoId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAllApartments() {
        parserService.deleteAllApartments();
    }

    @DeleteMapping("/{avitoId}")
    public ResponseEntity<Void> deleteApartment(@PathVariable String avitoId) {
        boolean deleted = parserService.deleteApartmentByAvitoId(avitoId);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    @PostMapping("/bot/start")
    public AvitoVisitorBotService.BotStatusDto startBot() {
        return botService.start();
    }

    @PostMapping("/bot/stop")
    public AvitoVisitorBotService.BotStatusDto stopBot() {
        return botService.stop();
    }

    @GetMapping("/bot/status")
    public AvitoVisitorBotService.BotStatusDto botStatus() {
        return botService.getStatus();
    }

    @GetMapping("/bot/queue")
    public List<BotQueueItem> getBotQueue() {
        return repository.findQueueForBot().stream()
                .map(a -> new BotQueueItem(a.getId(), a.getAvitoId(), a.getUrl(), a.getDetailVisitAttempts()))
                .toList();
    }

    @PostMapping("/{avitoId}/bot/visited")
    public ResponseEntity<Apartment> markVisited(
            @PathVariable String avitoId,
            @RequestBody BotVisitedRq body
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

    @PostMapping("/rank")
    public List<ApartmentScoreResult> rank(
            @RequestBody ScoringRq rq,
            @RequestParam(name = "limit", required = false) Integer limit
    ) {
        List<Apartment> apartments = repository.findAll();
        List<ApartmentScoreResult> ranked = scoringService.scoreAndRank(apartments, rq.weights());

        if (limit != null && limit > 0 && limit < ranked.size()) {
            return ranked.subList(0, limit);
        }
        return ranked;
    }
}