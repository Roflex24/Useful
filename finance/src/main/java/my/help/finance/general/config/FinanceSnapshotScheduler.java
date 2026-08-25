package my.help.finance.general.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.help.finance.general.service.FinanceSnapshotService;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@EnableScheduling
public class FinanceSnapshotScheduler {

    private final FinanceSnapshotService snapshotService;

    /**
     * Каждый день в 7:30 проверяем и создаём снимок если нужно
     */
    @Scheduled(cron = "0 30 7 * * *")
    public void checkAndCreateSnapshot() {
        if (snapshotService.shouldCreateSnapshot()) {
            log.info("Starting scheduled snapshot creation...");
            snapshotService.createSnapshotForPreviousMonth();
            log.info("Scheduled snapshot creation completed");
        }
    }
}