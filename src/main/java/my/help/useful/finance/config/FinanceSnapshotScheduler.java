package my.help.useful.finance.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.help.useful.finance.service.FinanceSnapshotService;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class FinanceSnapshotScheduler {

    private final FinanceSnapshotService snapshotService;

    /**
     * Каждый день в 03:00 проверяем и создаём снимок если нужно
     */
    @Scheduled(cron = "0 0 3 * * *")
    public void checkAndCreateSnapshot() {
        if (snapshotService.shouldCreateSnapshot()) {
            log.info("Starting scheduled snapshot creation...");
            snapshotService.createSnapshotForPreviousMonth();
            log.info("Scheduled snapshot creation completed");
        }
    }

    /**
     * При запуске приложения создаём начальный снимок (если нужно)
     */
    // @EventListener(ApplicationReadyEvent.class)
    public void onApplicationStart() {
        log.info("Checking for initial snapshot on application start...");
        snapshotService.createInitialSnapshot();
    }
}