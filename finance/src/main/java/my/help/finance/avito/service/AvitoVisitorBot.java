package my.help.finance.avito.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.KeyEvent;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class AvitoVisitorBot {

    private static final String BACKEND_BASE_URL = "http://localhost:8080";

    private static final int PAGE_LOAD_MIN_MS = 4_000;
    private static final int PAGE_LOAD_MAX_MS = 9_000;

    private static final int VIEW_SOURCE_LOAD_MIN_MS = 1_500;
    private static final int VIEW_SOURCE_LOAD_MAX_MS = 2_800;

    private static final int BETWEEN_ITEMS_MIN_MS = 20_000;
    private static final int BETWEEN_ITEMS_MAX_MS = 75_000;

    private static final double LONG_BREAK_CHANCE = 0.08;
    private static final int LONG_BREAK_MIN_MS = 3 * 60_000;
    private static final int LONG_BREAK_MAX_MS = 7 * 60_000;

    private static final int MIN_HTML_LENGTH = 2000;
    private static final int MAX_ATTEMPTS_PER_RUN = 3;

    private final Robot robot;
    private final HttpClient http = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    public AvitoVisitorBot() throws AWTException {
        if (GraphicsEnvironment.isHeadless()) {
            throw new IllegalStateException(
                    "Нет графической сессии (headless). Бот управляет настоящей мышью/клавиатурой " +
                            "и должен запускаться на десктопе, а не на сервере.");
        }
        this.robot = new Robot();
        this.robot.setAutoWaitForIdle(true);
        this.robot.setAutoDelay(35);
    }

    public static void main(String[] args) throws Exception {
        System.out.println("Авито-бот запускается.");
        System.out.println("Не трогайте мышь и клавиатуру, пока бот работает. Остановить — Ctrl+C в консоли.");
        sleep(3000);

        AvitoVisitorBot bot = new AvitoVisitorBot();
        bot.run();
    }

    private void run() throws Exception {
        List<QueueItem> queue = fetchQueue();
        System.out.println("В очереди объявлений: " + queue.size());

        int done = 0;
        for (QueueItem item : queue) {
            if (item.previousAttempts >= MAX_ATTEMPTS_PER_RUN) {
                System.out.println("Пропускаю " + item.avitoId + " — уже " + item.previousAttempts + " неудачных попыток");
                continue;
            }

            System.out.printf("[%d/%d] Объявление %s -> %s%n", ++done, queue.size(), item.avitoId, item.url);

            boolean success;
            try {
                String html = visitAndCapture(item);
                success = html != null;
                reportVisited(item.avitoId, success, html);
            } catch (Exception e) {
                System.out.println("  Ошибка при обходе: " + e.getMessage());
                reportVisited(item.avitoId, false, null);
                success = false;
            }

            System.out.println(success ? "  ✅ данные отправлены на backend" : "  ⚠️ не удалось скопировать страницу, попробуем в другой раз");

            waitBetweenItems();
        }

        System.out.println("Готово. Обработано объявлений: " + done);
    }

    private String visitAndCapture(QueueItem item) throws Exception {
        Desktop.getDesktop().browse(new URI(item.url));
        sleep(randomBetween(PAGE_LOAD_MIN_MS, PAGE_LOAD_MAX_MS));

        humanizeReading();

        chord(KeyEvent.VK_U);
        sleep(randomBetween(VIEW_SOURCE_LOAD_MIN_MS, VIEW_SOURCE_LOAD_MAX_MS));

        chord(KeyEvent.VK_A);
        sleep(randomBetween(200, 500));
        chord(KeyEvent.VK_C);
        sleep(randomBetween(400, 900));

        String html = readClipboardText();

        closeTab();
        sleep(randomBetween(300, 700));
        closeTab();

        if (html == null || html.length() < MIN_HTML_LENGTH) {
            return null;
        }
        return html;
    }

    private String readClipboardText() {
        try {
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            Object data = clipboard.getData(DataFlavor.stringFlavor);
            return data != null ? data.toString() : null;
        } catch (Exception e) {
            System.out.println("  Не удалось прочитать буфер обмена: " + e.getMessage());
            return null;
        }
    }

    private void humanizeReading() {
        int wiggles = randomBetween(2, 4);
        for (int i = 0; i < wiggles; i++) {
            Point p = MouseInfo.getPointerInfo().getLocation();
            int dx = randomBetween(-120, 120);
            int dy = randomBetween(-80, 80);
            moveMouseSmoothly(p.x, p.y, p.x + dx, p.y + dy);
            sleep(randomBetween(300, 900));

            if (ThreadLocalRandom.current().nextBoolean()) {
                robot.mouseWheel(randomBetween(1, 4));
                sleep(randomBetween(400, 1100));
            }
        }
    }

    private void moveMouseSmoothly(int fromX, int fromY, int toX, int toY) {
        int steps = randomBetween(8, 16);
        for (int i = 1; i <= steps; i++) {
            int x = fromX + (toX - fromX) * i / steps;
            int y = fromY + (toY - fromY) * i / steps;
            robot.mouseMove(x, y);
            sleep(randomBetween(8, 25));
        }
    }

    private void closeTab() {
        chord(KeyEvent.VK_W);
    }

    private void chord(int key) {
        robot.keyPress(KeyEvent.VK_CONTROL);
        robot.keyPress(key);
        robot.keyRelease(key);
        robot.keyRelease(KeyEvent.VK_CONTROL);
    }

    private void waitBetweenItems() {
        boolean longBreak = ThreadLocalRandom.current().nextDouble() < LONG_BREAK_CHANCE;
        int ms = longBreak
                ? randomBetween(LONG_BREAK_MIN_MS, LONG_BREAK_MAX_MS)
                : randomBetween(BETWEEN_ITEMS_MIN_MS, BETWEEN_ITEMS_MAX_MS);
        System.out.println("  … пауза " + (ms / 1000) + " сек" + (longBreak ? " (длинная)" : ""));
        sleep(ms);
    }

    private List<QueueItem> fetchQueue() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BACKEND_BASE_URL + "/api/apartments/bot/queue"))
                .GET()
                .build();
        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());

        if (resp.statusCode() != 200) {
            throw new IllegalStateException("Бэкенд вернул " + resp.statusCode() + ": " + resp.body());
        }

        List<QueueItem> result = new ArrayList<>();
        JsonNode arr = mapper.readTree(resp.body());
        for (JsonNode node : arr) {
            QueueItem item = new QueueItem();
            item.avitoId = node.get("avitoId").asText();
            item.url = node.get("url").asText();
            item.previousAttempts = node.hasNonNull("previousAttempts") ? node.get("previousAttempts").asInt() : 0;
            result.add(item);
        }
        return result;
    }

    private void reportVisited(String avitoId, boolean success, String html) {
        try {
            String body = mapper.writeValueAsString(new VisitedRequest(success, html));
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(BACKEND_BASE_URL + "/api/apartments/" + avitoId + "/bot/visited"))
                    .timeout(Duration.ofSeconds(20))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();
            http.send(req, HttpResponse.BodyHandlers.discarding());
        } catch (Exception e) {
            System.out.println("  Не удалось отчитаться бэкенду по " + avitoId + ": " + e.getMessage());
        }
    }

    private static int randomBetween(int minInclusive, int maxInclusive) {
        return ThreadLocalRandom.current().nextInt(minInclusive, maxInclusive + 1);
    }

    private static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static class QueueItem {
        String avitoId;
        String url;
        int previousAttempts;
    }

    private record VisitedRequest(boolean success, String html) {}
}