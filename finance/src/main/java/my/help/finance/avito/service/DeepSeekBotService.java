package my.help.finance.avito.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;
import java.net.URI;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Робот, который открывает чат DeepSeek в браузере,
 * вставляет туда готовый промпт и отправляет его.

 * Управляет настоящей мышью/клавиатурой — backend должен запускаться
 * на десктопе с монитором (java.awt.headless=false).
 */
@Slf4j
@Service
public class DeepSeekBotService {

    private static final int PAGE_LOAD_MIN_MS = 5_000;
    private static final int PAGE_LOAD_MAX_MS = 9_000;
    private static final int AFTER_PASTE_MS    = 900;
    private static final int AFTER_SUBMIT_MS   = 1_500;

    /** URL чата DeepSeek. Можно переопределить через application.yml. */
    @Value("${deepseek.chat.url:https://chat.deepseek.com/}")
    private String deepSeekUrl;

    private final ApartmentAnalysisService analysisService;

    private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "deepseek-bot");
        t.setDaemon(true);
        return t;
    });

    public enum Status { IDLE, RUNNING, STOPPING }

    public record BotStatusDto(
            Status status,
            Integer promptLength,
            String lastError,
            String chatUrl
    ) {}

    private volatile Status status = Status.IDLE;
    private volatile String lastError;
    private volatile Integer promptLength;
    private final AtomicBoolean stopRequested = new AtomicBoolean(false);
    private volatile Future<?> runningTask;

    public DeepSeekBotService(ApartmentAnalysisService analysisService) {
        this.analysisService = analysisService;
    }

    public synchronized BotStatusDto start() {
        return start(null);
    }

    /**
     * @param promptOverride готовый промпт с фронта (если null — генерируется автоматически из БД)
     */
    public synchronized BotStatusDto start(String promptOverride) {
        if (status == Status.RUNNING || status == Status.STOPPING) {
            return getStatus();
        }
        if (GraphicsEnvironment.isHeadless()) {
            lastError = "Нет графической сессии (headless). Бот управляет настоящей мышью/клавиатурой — "
                    + "запускайте backend на десктопе.";
            return getStatus();
        }

        stopRequested.set(false);
        lastError = null;
        promptLength = null;
        status = Status.RUNNING;

        final String override = promptOverride;
        runningTask = executor.submit(() -> runLoop(override));
        return getStatus();
    }

    public synchronized BotStatusDto stop() {
        if (status == Status.RUNNING) {
            status = Status.STOPPING;
            stopRequested.set(true);
            if (runningTask != null) runningTask.cancel(true);
        }
        return getStatus();
    }

    public BotStatusDto getStatus() {
        return new BotStatusDto(status, promptLength, lastError, deepSeekUrl);
    }

    private void runLoop(String promptOverride) {
        try {
            Robot robot = new Robot();
            robot.setAutoWaitForIdle(true);
            robot.setAutoDelay(35);

            String prompt = (promptOverride != null && !promptOverride.isBlank())
                    ? promptOverride
                    : analysisService.buildAnalysisPrompt();

            promptLength = prompt.length();
            log.info("DeepSeek-бот: промпт длиной {} символов", promptLength);

            if (stopRequested.get()) return;

            log.info("DeepSeek-бот: открываю чат {}", deepSeekUrl);
            Desktop.getDesktop().browse(new URI(deepSeekUrl));
            sleep(randomBetween());

            if (stopRequested.get()) return;

            // кладём промпт в системный буфер обмена
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(new StringSelection(prompt), null);
            log.info("DeepSeek-бот: промпт помещён в буфер обмена");

            log.info("DeepSeek-бот: вставляю промпт (Ctrl+V)…");
            chord(robot);
            sleep(AFTER_PASTE_MS);

            if (stopRequested.get()) return;

            log.info("DeepSeek-бот: отправляю (Enter)…");
            robot.keyPress(KeyEvent.VK_ENTER);
            robot.keyRelease(KeyEvent.VK_ENTER);
            sleep(AFTER_SUBMIT_MS);

            log.info("DeepSeek-бот: промпт отправлен в чат DeepSeek");

        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            log.info("DeepSeek-бот: прервано пользователем");
        } catch (Exception e) {
            lastError = "Ошибка DeepSeek-бота: " + e.getMessage();
            log.error(lastError, e);
        } finally {
            status = Status.IDLE;
            stopRequested.set(false);
        }
    }

    private void chord(Robot robot) {
        robot.keyPress(KeyEvent.VK_CONTROL);
        robot.keyPress(KeyEvent.VK_V);
        robot.keyRelease(KeyEvent.VK_V);
        robot.keyRelease(KeyEvent.VK_CONTROL);
    }

    private static int randomBetween() {
        return ThreadLocalRandom.current().nextInt(DeepSeekBotService.PAGE_LOAD_MIN_MS, DeepSeekBotService.PAGE_LOAD_MAX_MS + 1);
    }

    private static void sleep(long ms) throws InterruptedException {
        Thread.sleep(ms);
    }
}