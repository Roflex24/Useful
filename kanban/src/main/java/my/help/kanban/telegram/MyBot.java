package my.help.kanban.telegram;

import lombok.RequiredArgsConstructor;
import my.help.kanban.event.EventService;
import my.help.kanban.planning.service.PlanTaskService;
import my.help.kanban.task.TaskService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(name = "telegram.enabled", havingValue = "true")
@Component
@RequiredArgsConstructor
public class MyBot extends TelegramLongPollingBot {

    @org.springframework.beans.factory.annotation.Value("${telegram.username}")
    private String botUsername;
    @org.springframework.beans.factory.annotation.Value("${telegram.token}")
    private String botToken;

    private final PlanTaskService planTaskService;
    private final TaskService taskService;
    private final EventService eventService;

    private static final String CALLBACK_TASKS = "show_tasks";
    private static final String CALLBACK_HELP  = "show_help";
    private static final String CALLBACK_ABOUT = "show_about";

    // 1. Переопределяем метод для получения имени бота
    @Override
    public String getBotUsername() {
        return botUsername;
    }

    // 2. Переопределяем метод для получения токена
    @Override
    public String getBotToken() {
        return botToken;
    }

    // 3. Главный метод, который обрабатывает все входящие сообщения
    @Override
    public void onUpdateReceived(Update update) {

        // --- Обработка нажатия inline-кнопок ---
        if (update.hasCallbackQuery()) {
            String callbackData = update.getCallbackQuery().getData();
            long chatId = update.getCallbackQuery().getMessage().getChatId();
            String text;

            switch (callbackData) {
                case CALLBACK_TASKS:
                    text = planTaskService.getPlanTasksForTelegramBot();
                    break;
                case CALLBACK_HELP:
                    text = "Доступные команды:\n" +
                            "/start — главное меню\n" +
                            "📅 Планы на неделю — список задач на неделю\n" +
                            "✅ Актуальные задачи — задачи в работе\n" +
                            "🔔 Предстоящие события — ближайшие события\n" +
                            "❓ Помощь — эта справка";
                    break;
                case CALLBACK_ABOUT:
                    text = "🤖 Я Java-бот, помогаю управлять задачами и проектами.";
                    break;
                default:
                    text = "⚠️ Неизвестная команда";
            }

            SendMessage message = new SendMessage();
            message.setChatId(String.valueOf(chatId));
            message.setText(text);

            try {
                execute(message);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
            return;
        }

        // --- Обработка обычных сообщений ---
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            SendMessage message = new SendMessage();
            message.setChatId(String.valueOf(chatId));

            switch (messageText) {
                case "/start":
                    message.setText("👋 Привет! Я Java-бот. Чем могу помочь?");
                    // Клавиатура с постоянными кнопками внизу
                    message.setReplyMarkup(createReplyKeyboard());
                    break;

                case "📅 Планы на неделю":
                    message.setText(planTaskService.getPlanTasksForTelegramBot());
                    break;

                case "✅ Актуальные задачи":
                    message.setText(taskService.getTasksForColumnInWork());
                    break;

                case "🔔 Предстоящие события":
                    message.setText(eventService.getEventsForTelegram());
                    break;

                case "🎛️ Inline-меню":
                    // Пример inline-кнопок, прикреплённых к сообщению
                    message.setText("🎛️ Inline-меню:");
                    message.setReplyMarkup(createInlineKeyboard());
                    break;

                default:
                    message.setText("⚠️ Неизвестная команда. Нажми /start");
            }

            try {
                execute(message);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Reply-клавиатура — кнопки внизу экрана, всегда видны пользователю.
     */
    private ReplyKeyboardMarkup createReplyKeyboard() {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);   // подгоняет размер под кнопки
        keyboardMarkup.setOneTimeKeyboard(false); // не скрывать после нажатия

        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow row1 = new KeyboardRow();
        row1.add(new KeyboardButton("📅 Планы на неделю"));
        row1.add(new KeyboardButton("✅ Актуальные задачи"));
        row1.add(new KeyboardButton("🔔 Предстоящие события"));

        KeyboardRow row2 = new KeyboardRow();
        row2.add(new KeyboardButton("🎛️ Inline-меню"));

        keyboard.add(row1);
        keyboard.add(row2);

        keyboardMarkup.setKeyboard(keyboard);
        return keyboardMarkup;
    }

    /**
     * Inline-клавиатура — кнопки под сообщением, обрабатываются через callback.
     */
    private InlineKeyboardMarkup createInlineKeyboard() {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();

        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        row1.add(InlineKeyboardButton.builder()
                .text("📝 Показать задачи")
                .callbackData(CALLBACK_TASKS)
                .build());

        List<InlineKeyboardButton> row2 = new ArrayList<>();
        row2.add(InlineKeyboardButton.builder()
                .text("❓ Помощь")
                .callbackData(CALLBACK_HELP)
                .build());
        row2.add(InlineKeyboardButton.builder()
                .text("🤖 О боте")
                .callbackData(CALLBACK_ABOUT)
                .build());

        rows.add(row1);
        rows.add(row2);
        markup.setKeyboard(rows);

        return markup;
    }
}