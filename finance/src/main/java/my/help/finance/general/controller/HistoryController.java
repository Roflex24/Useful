package my.help.finance.general.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.help.finance.general.service.FinanceSnapshotService;
import my.help.finance.general.dto.HistoricalDataResponseDto;
import my.help.finance.general.dto.MonthlyDynamicsDto;
import my.help.finance.general.dto.SnapshotInfoDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/finance/history")
@RequiredArgsConstructor
public class HistoryController {

    private final FinanceSnapshotService snapshotService;

    /**
     * Получить список доступных снимков (месяцев с данными)
     * GET /api/finance/history/snapshots
     */
    @GetMapping("/snapshots")
    public List<SnapshotInfoDto> getAvailableSnapshots() {
        return snapshotService.getAvailableSnapshots();
    }

    /**
     * Получить данные за конкретный месяц
     * GET /api/finance/history/data?year=2026&month=1
     * GET /api/finance/history/data/2026-01
     */
    @GetMapping("/data")
    public ResponseEntity<HistoricalDataResponseDto> getHistoricalData(
            @RequestParam int year,
            @RequestParam int month
    ) {
        YearMonth yearMonth = YearMonth.of(year, month);
        HistoricalDataResponseDto data = snapshotService.getHistoricalData(yearMonth);

        if (data == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(data);
    }

    @GetMapping("/data/{yearMonth}")
    public ResponseEntity<HistoricalDataResponseDto> getHistoricalDataByPath(
            @PathVariable String yearMonth
    ) {
        try {
            YearMonth ym = YearMonth.parse(yearMonth);
            HistoricalDataResponseDto data = snapshotService.getHistoricalData(ym);

            if (data == null) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(data);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Принудительно создать снимок (для администрирования)
     * POST /api/finance/history/snapshots/create
     */
    @PostMapping("/snapshots/create")
    public ResponseEntity<String> createSnapshotManually() {
        try {
            snapshotService.createSnapshotForPreviousMonth();
            return ResponseEntity.ok("Snapshot created successfully");
        } catch (Exception e) {
            log.error("Failed to create snapshot", e);
            return ResponseEntity.internalServerError().body("Failed: " + e.getMessage());
        }
    }

    /**
     * Получить помесячную динамику
     * GET /api/finance/history/dynamics/monthly
     */
    @GetMapping("/dynamics/monthly")
    public List<MonthlyDynamicsDto> getMonthlyDynamics() {
        return snapshotService.getMonthlyDynamics();
    }
}