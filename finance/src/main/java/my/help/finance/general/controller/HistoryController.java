package my.help.finance.general.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.help.finance.common.exception.ResourceNotFoundException;
import my.help.finance.general.service.FinanceSnapshotService;
import my.help.finance.general.dto.HistoricalDataResponseDto;
import my.help.finance.general.dto.MonthlyDynamicsDto;
import my.help.finance.general.dto.SnapshotInfoDto;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/finance/history")
@RequiredArgsConstructor
@Tag(name = "Finance API", description = "Раздел финансов")
public class HistoryController {

    private final FinanceSnapshotService snapshotService;

    @GetMapping("/snapshots")
    public List<SnapshotInfoDto> getAvailableSnapshots() {
        return snapshotService.getAvailableSnapshots();
    }

    @GetMapping("/data")
    public HistoricalDataResponseDto getHistoricalData(
            @RequestParam int year,
            @RequestParam int month
    ) {
        YearMonth yearMonth = YearMonth.of(year, month);
        HistoricalDataResponseDto data = snapshotService.getHistoricalData(yearMonth);
        if (data == null) {
            throw new ResourceNotFoundException("No snapshot found for " + yearMonth);
        }
        return data;
    }

    @GetMapping("/data/{yearMonth}")
    public HistoricalDataResponseDto getHistoricalDataByPath(
            @PathVariable String yearMonth
    ) {
        YearMonth ym = YearMonth.parse(yearMonth);
        HistoricalDataResponseDto data = snapshotService.getHistoricalData(ym);
        if (data == null) {
            throw new ResourceNotFoundException("No snapshot found for " + ym);
        }
        return data;
    }

    @PostMapping("/snapshots/create")
    public String createSnapshotManually() {
        snapshotService.createSnapshotForPreviousMonth();
        return "Snapshot created successfully";
    }

    @GetMapping("/dynamics/monthly")
    public List<MonthlyDynamicsDto> getMonthlyDynamics() {
        return snapshotService.getMonthlyDynamics();
    }
}