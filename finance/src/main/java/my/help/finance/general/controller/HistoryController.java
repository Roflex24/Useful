package my.help.finance.general.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.help.finance.common.ResourceNotFoundException;
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

    @GetMapping("/snapshots")
    public List<SnapshotInfoDto> getAvailableSnapshots() {
        return snapshotService.getAvailableSnapshots();
    }

    @GetMapping("/data")
    public ResponseEntity<HistoricalDataResponseDto> getHistoricalData(
            @RequestParam int year,
            @RequestParam int month
    ) {
        YearMonth yearMonth = YearMonth.of(year, month);
        HistoricalDataResponseDto data = snapshotService.getHistoricalData(yearMonth);
        if (data == null) {
            throw new ResourceNotFoundException("No snapshot found for " + yearMonth);
        }
        return ResponseEntity.ok(data);
    }

    @GetMapping("/data/{yearMonth}")
    public ResponseEntity<HistoricalDataResponseDto> getHistoricalDataByPath(
            @PathVariable String yearMonth
    ) {
        YearMonth ym = YearMonth.parse(yearMonth);
        HistoricalDataResponseDto data = snapshotService.getHistoricalData(ym);
        if (data == null) {
            throw new ResourceNotFoundException("No snapshot found for " + ym);
        }
        return ResponseEntity.ok(data);
    }

    @PostMapping("/snapshots/create")
    public ResponseEntity<String> createSnapshotManually() {
        snapshotService.createSnapshotForPreviousMonth();
        return ResponseEntity.ok("Snapshot created successfully");
    }

    @GetMapping("/dynamics/monthly")
    public List<MonthlyDynamicsDto> getMonthlyDynamics() {
        return snapshotService.getMonthlyDynamics();
    }
}