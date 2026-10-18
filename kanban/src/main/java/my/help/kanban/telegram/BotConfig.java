package my.help.kanban.telegram;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(name = "telegram.enabled", havingValue = "true")
@Configuration
@Slf4j
public class BotConfig {

    public BotConfig(MyBot bot) {
        try {
            TelegramBotsApi api = new TelegramBotsApi(DefaultBotSession.class);
            api.registerBot(bot);
        } catch (Exception e) {
            log.error("Не удалось подключиться к телеграмм боту");
        }
    }
}