package my.help.finance.avito.service;

import lombok.extern.slf4j.Slf4j;
import my.help.finance.avito.entity.Apartment;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.KeyEvent;
import java.net.URI;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Робот, который сам открывает страницу поиска Авито в браузере,
 * человекоподобно проматывает её до конца, открывает исходник (Ctrl+U),
 * копирует HTML и передаёт его в существующий парсер.
 *
 * Управляет настоящей мышью/клавиатурой — backend должен запускаться
 * на десктопе с монитором (java.awt.headless=false).
 */
@Slf4j
@Service
public class AvitoSearchPageBotService {

    // Загрузка страницы поиска — она тяжёлая, ждём дольше чем у детальной
    private static final int PAGE_LOAD_MIN_MS = 6_000;
    private static final int PAGE_LOAD_MAX_MS = 12_000;

    // view-source тоже рендерится какое-то время
    private static final int VIEW_SOURCE_LOAD_MIN_MS = 2_500;
    private static final int VIEW_SOURCE_LOAD_MAX_MS = 4_500;

    // Минимальная длина HTML, которую считаем «настоящей страницей»
    private static final int MIN_HTML_LENGTH = 5_000;

    /**
     * URL страницы поиска. Можно переопределить через application.yml:
     *   avito:
     *     search:
     *       url: "https://www.avito.ru/..."
     */
    @Value("${avito.search.url:https://www.avito.ru/nizhniy_novgorod/kvartiry/prodam}")
    private String defaultSearchUrl;

    private final AvitoParserService parserService;

    private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "avito-search-bot");
        t.setDaemon(true);
        return t;
    });

    private volatile Status status = Status.IDLE;
    private volatile String lastError;
    private volatile Integer parsedCount;
    private volatile String activeUrl;
    private final AtomicBoolean stopRequested = new AtomicBoolean(false);
    private volatile Future<?> runningTask;

    public AvitoSearchPageBotService(AvitoParserService parserService) {
        this.parserService = parserService;
    }

    public enum Status { IDLE, RUNNING, STOPPING }

    public record BotStatusDto(
            Status status,
            Integer parsedCount,
            String lastError,
            String searchUrl
    ) {}

    /** Запуск с URL по умолчанию (из конфига). */
    public synchronized BotStatusDto start() {
        return start(null);
    }

    /** Запуск с произвольным URL (передаётся с фронта — там уже есть ссылка на поиск). */
    public synchronized BotStatusDto start(String urlOverride) {
        if (status == Status.RUNNING || status == Status.STOPPING) {
            return getStatus();
        }
        if (GraphicsEnvironment.isHeadless()) {
            lastError = "Нет графической сессии (headless). Бот управляет настоящей мышью/клавиатурой — " +
                    "запускайте backend на десктопе с монитором (System.setProperty(\"java.awt.headless\",\"false\") " +
                    "до SpringApplication.run).";
            return getStatus();
        }

        this.activeUrl = (urlOverride != null && !urlOverride.isBlank())
                ? urlOverride
                : defaultSearchUrl;

        stopRequested.set(false);
        lastError = null;
        parsedCount = null;
        status = Status.RUNNING;
        runningTask = executor.submit(this::runLoop);
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
        return new BotStatusDto(status, parsedCount, lastError, activeUrl);
    }

    // ------------------------------------------------------------------

    private void runLoop() {
        try {
            Robot robot = new Robot();
            robot.setAutoWaitForIdle(true);
            robot.setAutoDelay(35);

            log.info("Авто-загрузка: открываю страницу поиска {}", activeUrl);
            Desktop.getDesktop().browse(new URI(activeUrl));
            sleep(randomBetween(PAGE_LOAD_MIN_MS, PAGE_LOAD_MAX_MS));

            if (stopRequested.get()) return;

            log.info("Авто-загрузка: человекоподобно проматываю страницу до низа…");
            scrollToBottomHumanLike(robot);

            if (stopRequested.get()) return;

            log.info("Авто-загрузка: открываю исходник страницы (Ctrl+U)…");
            chord(robot, KeyEvent.VK_U);
            sleep(randomBetween(VIEW_SOURCE_LOAD_MIN_MS, VIEW_SOURCE_LOAD_MAX_MS));

            if (stopRequested.get()) { closeTab(robot); return; }

            log.info("Авто-загрузка: выделяю всё (Ctrl+A)…");
            chord(robot, KeyEvent.VK_A);
            sleep(randomBetween(250, 600));

            log.info("Авто-загрузка: копирую (Ctrl+C)…");
            chord(robot, KeyEvent.VK_C);
            sleep(randomBetween(700, 1_500)); // большая страница — даём время на копирование

            String html = readClipboardText();

            // Закрываем view-source и вкладку с поиском
            closeTab(robot);
            sleep(randomBetween(400, 900));
            closeTab(robot);

            if (html == null || html.length() < MIN_HTML_LENGTH) {
                lastError = "Не удалось скопировать HTML страницы (получено "
                        + (html == null ? 0 : html.length()) + " символов).";
                log.warn(lastError);
                return;
            }

            log.info("Авто-загрузка: получен HTML длиной {} символов, отдаю в парсер…", html.length());
            List<Apartment> saved = parserService.parseAndSaveMultiple(List.of(html));
            parsedCount = saved.size();
            log.info("Авто-загрузка: сохранено/обновлено объявлений: {}", parsedCount);

        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            log.info("Авто-загрузка: прервано пользователем");
        } catch (Exception e) {
            lastError = "Ошибка автоматической загрузки: " + e.getMessage();
            log.error(lastError, e);
        } finally {
            status = Status.IDLE;
            stopRequested.set(false);
            log.info("Авто-загрузка завершена. parsedCount={}, lastError={}", parsedCount, lastError);
        }
    }

    // ------------------------------------------------------------------
    //  Человекоподобный скролл
    // ------------------------------------------------------------------

    private void scrollToBottomHumanLike(Robot robot) throws InterruptedException {
        // Человек «осмотрел шапку страницы», повозил мышью
        sleep(randomBetween(1_500, 3_500));
        wiggleMouse(robot);
        sleep(randomBetween(800, 1_800));

        // Общий бюджет прокрутки (в «щелчках» колеса).
        // Avito подгружает ~50 карточек; 220–300 «щелчков» с запасом хватает.
        int totalClicks = randomBetween(220, 300);
        int done = 0;

        while (done < totalClicks) {
            if (stopRequested.get()) return;

            // Небольшая порция прокрутки
            int chunk = randomBetween(3, 8);
            for (int i = 0; i < chunk && done < totalClicks; i++) {
                int wheel = randomBetween(3, 7);
                robot.mouseWheel(wheel);
                done += wheel;
                sleep(randomBetween(280, 800));
            }

            // Пауза «почитать карточки»
            if (ThreadLocalRandom.current().nextDouble() < 0.55) {
                sleep(randomBetween(900, 2_800));
            }

            // «Перечитал — откатился назад»
            if (ThreadLocalRandom.current().nextDouble() < 0.14) {
                robot.mouseWheel(-randomBetween(2, 5));
                sleep(randomBetween(500, 1_200));
            }

            // Подвигать мышью — как будто следишь взглядом за скроллом
            if (ThreadLocalRandom.current().nextDouble() < 0.35) {
                wiggleMouse(robot);
            }
        }

        // В самом низу — «дочитал, осмыслил»
        sleep(randomBetween(2_000, 4_500));
        wiggleMouse(robot);
        sleep(randomBetween(500, 1_200));
    }

    private void wiggleMouse(Robot robot) throws InterruptedException {
        try {
            Point p = MouseInfo.getPointerInfo().getLocation();
            int dx = randomBetween(-140, 140);
            int dy = randomBetween(-100, 100);
            moveMouseSmoothly(robot, p.x, p.y, p.x + dx, p.y + dy);
            sleep(randomBetween(200, 600));
        } catch (Exception ignored) {
            // MouseInfo иногда недоступен — не критично
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
    //  Клавиатура и буфер обмена
    // ------------------------------------------------------------------

    private void closeTab(Robot robot) {
        chord(robot, KeyEvent.VK_W);
    }

    private void chord(Robot robot, int key) {
        robot.keyPress(KeyEvent.VK_CONTROL);
        robot.keyPress(key);
        robot.keyRelease(key);
        robot.keyRelease(KeyEvent.VK_CONTROL);
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

    private static int randomBetween(int minInclusive, int maxInclusive) {
        return ThreadLocalRandom.current().nextInt(minInclusive, maxInclusive + 1);
    }

    private static void sleep(long ms) throws InterruptedException {
        Thread.sleep(ms);
    }
}