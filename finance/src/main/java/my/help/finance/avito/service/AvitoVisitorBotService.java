package my.help.finance.avito.service;

import lombok.extern.slf4j.Slf4j;
import my.help.finance.avito.entity.Apartment;
import my.help.finance.avito.repository.ApartmentRepository;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.KeyEvent;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Бот-обходчик объявлений, встроенный в backend — запускается и
 * останавливается кнопкой на странице avito.html.
 * <p>
 * Ничего не скачивает на диск. По каждому объявлению: открывает страницу
 * в настоящем браузере (java.awt.Desktop), "читает" её (случайные движения
 * мыши/прокрутка через java.awt.Robot — то самое подобие живого человека),
 * затем жмёт Ctrl+U ("Просмотр кода страницы"), выделяет всё (Ctrl+A) и
 * копирует в буфер обмена (Ctrl+C). Дальше исходник страницы читается из
 * системного буфера обмена и парсится {@link AvitoDetailPageParserService}
 * напрямую в поля {@link Apartment} — файл на диске не создаётся вообще.
 * <p>
 * ВАЖНО: backend должен работать на десктопе с монитором, а не в
 * headless-окружении. Spring Boot по умолчанию выставляет
 * java.awt.headless=true — это нужно переопределить ДО старта Spring
 * (см. javadoc в главном классе приложения), иначе Robot не создастся.
 * <p>
 * Пока бот работает — не трогайте мышь и клавиатуру сами, иначе он
 * собьётся с вкладкой просмотра кода или скопирует не то.
 */
@Slf4j
@Service
public class AvitoVisitorBotService {

    // ------------------------------------------------------------------
    // Конфигурация
    // ------------------------------------------------------------------

    private static final int PAGE_LOAD_MIN_MS = 4_000;
    private static final int PAGE_LOAD_MAX_MS = 9_000;

    private static final int VIEW_SOURCE_LOAD_MIN_MS = 1_500;
    private static final int VIEW_SOURCE_LOAD_MAX_MS = 2_800;

    private static final int BETWEEN_ITEMS_MIN_MS = 20_000;
    private static final int BETWEEN_ITEMS_MAX_MS = 75_000;

    private static final double LONG_BREAK_CHANCE = 0.08;
    private static final int LONG_BREAK_MIN_MS = 3 * 60_000;
    private static final int LONG_BREAK_MAX_MS = 7 * 60_000;

    private static final int MAX_ATTEMPTS_PER_RUN = 3;

    /** Ниже этой длины скопированный текст точно не похож на исходник страницы объявления. */
    private static final int MIN_HTML_LENGTH = 2000;

    // ------------------------------------------------------------------

    private final ApartmentRepository repository;
    private final AvitoDetailPageParserService detailParser;

    private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "avito-visitor-bot");
        t.setDaemon(true);
        return t;
    });

    private volatile Status status = Status.IDLE;
    private volatile String currentAvitoId;
    private volatile String currentUrl;
    private volatile String lastError;
    private final AtomicInteger totalCount = new AtomicInteger(0);
    private final AtomicInteger processedCount = new AtomicInteger(0);
    private final AtomicBoolean stopRequested = new AtomicBoolean(false);
    private volatile Future<?> runningTask;

    public AvitoVisitorBotService(ApartmentRepository repository, AvitoDetailPageParserService detailParser) {
        this.repository = repository;
        this.detailParser = detailParser;
    }

    public enum Status { IDLE, RUNNING, STOPPING }

    /** Снимок состояния бота, отдаётся фронтенду для отрисовки прогресса. */
    public record BotStatusDto(
            Status status,
            int total,
            int processed,
            String currentAvitoId,
            String currentUrl,
            String lastError
    ) {}

    // ------------------------------------------------------------------
    // Публичное API (вызывается контроллером)
    // ------------------------------------------------------------------

    public synchronized BotStatusDto start() {
        if (status == Status.RUNNING || status == Status.STOPPING) {
            return getStatus();
        }
        if (GraphicsEnvironment.isHeadless()) {
            lastError = "Нет графической сессии (headless). Бот управляет настоящей мышью/клавиатурой — " +
                    "запускайте backend на десктопе с монитором (см. javadoc в главном классе приложения " +
                    "про System.setProperty(\"java.awt.headless\",\"false\") до SpringApplication.run).";
            return getStatus();
        }

        stopRequested.set(false);
        lastError = null;
        processedCount.set(0);
        totalCount.set(0);
        status = Status.RUNNING;

        runningTask = executor.submit(this::runLoop);
        return getStatus();
    }

    public synchronized BotStatusDto stop() {
        if (status == Status.RUNNING) {
            status = Status.STOPPING;
            stopRequested.set(true);
            if (runningTask != null) {
                runningTask.cancel(true); // прерывает текущий Thread.sleep — бот остановится между шагами
            }
        }
        return getStatus();
    }

    public BotStatusDto getStatus() {
        return new BotStatusDto(status, totalCount.get(), processedCount.get(), currentAvitoId, currentUrl, lastError);
    }

    // ------------------------------------------------------------------
    // Основной цикл (выполняется в фоновом потоке executor'а)
    // ------------------------------------------------------------------

    private void runLoop() {
        try {
            Robot robot = new Robot();
            robot.setAutoWaitForIdle(true);
            robot.setAutoDelay(35);

            List<Apartment> queue = repository.findQueueForBot();
            totalCount.set(queue.size());
            log.info("Бот стартовал, объявлений в очереди: {}", queue.size());

            for (Apartment apt : queue) {
                if (stopRequested.get() || Thread.currentThread().isInterrupted()) break;

                int attempts = apt.getDetailVisitAttempts() == null ? 0 : apt.getDetailVisitAttempts();
                if (attempts >= MAX_ATTEMPTS_PER_RUN) {
                    processedCount.incrementAndGet();
                    continue;
                }

                currentAvitoId = apt.getAvitoId();
                currentUrl = apt.getUrl();
                log.info("Обход объявления {} -> {}", currentAvitoId, currentUrl);

                String html = null;
                try {
                    html = visitAndCapture(robot, apt);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    log.warn("Ошибка при обходе {}: {}", apt.getAvitoId(), e.getMessage());
                }

                enrichAndSave(apt, html);
                processedCount.incrementAndGet();

                if (stopRequested.get()) break;
                try {
                    waitBetweenItems();
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        } catch (AWTException e) {
            lastError = "Не удалось создать Robot: " + e.getMessage();
            log.error(lastError, e);
        } catch (Exception e) {
            lastError = "Бот упал с ошибкой: " + e.getMessage();
            log.error(lastError, e);
        } finally {
            status = Status.IDLE;
            currentAvitoId = null;
            currentUrl = null;
            log.info("Бот остановлен. Обработано: {}/{}", processedCount.get(), totalCount.get());
        }
    }

    /** Парсит скопированный HTML (если есть) в поля квартиры и сохраняет через репозиторий. */
    private void enrichAndSave(Apartment apt, String html) {
        int attempts = apt.getDetailVisitAttempts() == null ? 0 : apt.getDetailVisitAttempts();
        apt.setDetailVisitAttempts(attempts + 1);

        boolean success = false;
        if (html != null) {
            try {
                success = detailParser.enrichFromDetailHtml(apt, html);
            } catch (Exception e) {
                log.warn("Ошибка парсинга детальной страницы {}: {}", apt.getAvitoId(), e.getMessage());
            }
        }

        if (success) {
            apt.setDetailVisited(true);
            apt.setDetailVisitedAt(LocalDateTime.now());
        }

        repository.save(apt);
        log.info(success ? "  ✅ {} — данные обновлены" : "  ⚠️ {} — не удалось получить данные, попробуем позже",
                apt.getAvitoId());
    }

    // ------------------------------------------------------------------
    // Один визит на страницу объявления
    // ------------------------------------------------------------------

    /** Открывает объявление, копирует исходник страницы через буфер обмена. Возвращает html либо null. */
    private String visitAndCapture(Robot robot, Apartment apt) throws Exception {
        Desktop.getDesktop().browse(new URI(apt.getUrl()));
        sleep(randomBetween(PAGE_LOAD_MIN_MS, PAGE_LOAD_MAX_MS));

        humanizeReading(robot);

        chord(robot, KeyEvent.VK_U); // "Просмотр кода страницы" — открывает новую вкладку
        sleep(randomBetween(VIEW_SOURCE_LOAD_MIN_MS, VIEW_SOURCE_LOAD_MAX_MS));

        chord(robot, KeyEvent.VK_A); // выделить весь текст исходника
        sleep(randomBetween(200, 500));
        chord(robot, KeyEvent.VK_C); // скопировать в буфер обмена
        sleep(randomBetween(400, 900));

        String html = readClipboardText();

        closeTab(robot); // закрыть вкладку с исходником
        sleep(randomBetween(300, 700));
        closeTab(robot); // закрыть вкладку с самим объявлением — с ним покончено

        if (html == null || html.length() < MIN_HTML_LENGTH) {
            return null; // скопировалось явно не то (капча, пустая страница и т.п.)
        }
        return html;
    }

    private String readClipboardText() {
        try {
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            Object data = clipboard.getData(DataFlavor.stringFlavor);
            return data != null ? data.toString() : null;
        } catch (Exception e) {
            log.warn("Не удалось прочитать буфер обмена: {}", e.getMessage());
            return null;
        }
    }

    private void humanizeReading(Robot robot) throws InterruptedException {
        int wiggles = randomBetween(2, 4);
        for (int i = 0; i < wiggles; i++) {
            Point p = MouseInfo.getPointerInfo().getLocation();
            int dx = randomBetween(-120, 120);
            int dy = randomBetween(-80, 80);
            moveMouseSmoothly(robot, p.x, p.y, p.x + dx, p.y + dy);
            sleep(randomBetween(300, 900));

            if (ThreadLocalRandom.current().nextBoolean()) {
                robot.mouseWheel(randomBetween(1, 4));
                sleep(randomBetween(400, 1100));
            }
        }
    }

    private void moveMouseSmoothly(Robot robot, int fromX, int fromY, int toX, int toY) throws InterruptedException {
        int steps = randomBetween(8, 16);
        for (int i = 1; i <= steps; i++) {
            int x = fromX + (toX - fromX) * i / steps;
            int y = fromY + (toY - fromY) * i / steps;
            robot.mouseMove(x, y);
            sleep(randomBetween(8, 25));
        }
    }

    // ------------------------------------------------------------------
    // Клавиатурные комбинации
    // ------------------------------------------------------------------

    private void closeTab(Robot robot) {
        chord(robot, KeyEvent.VK_W); // macOS: VK_META
    }

    private void chord(Robot robot, int key) {
        robot.keyPress(KeyEvent.VK_CONTROL);
        robot.keyPress(key);
        robot.keyRelease(key);
        robot.keyRelease(KeyEvent.VK_CONTROL);
    }

    private void waitBetweenItems() throws InterruptedException {
        boolean longBreak = ThreadLocalRandom.current().nextDouble() < LONG_BREAK_CHANCE;
        int ms = longBreak
                ? randomBetween(LONG_BREAK_MIN_MS, LONG_BREAK_MAX_MS)
                : randomBetween(BETWEEN_ITEMS_MIN_MS, BETWEEN_ITEMS_MAX_MS);
        sleep(ms);
    }

    private static int randomBetween(int minInclusive, int maxInclusive) {
        return ThreadLocalRandom.current().nextInt(minInclusive, maxInclusive + 1);
    }

    private static void sleep(long ms) throws InterruptedException {
        Thread.sleep(ms);
    }
}