package my.help.useful.ai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.Arrays;
import java.util.List;

record ChatRequest(String prompt) {}
record HistoryMessage(String role, String content) {}

@RestController
public class ChatController {

    private final ChatClient chatClient;
    private final ChatMemory chatMemory;

    public ChatController(ChatClient.Builder builder,
                          ChatMemory chatMemory,
                          List<ToolCallbackProvider> toolProviders) {

        ToolCallback[] tools = toolProviders.stream()
                .flatMap(p -> Arrays.stream(p.getToolCallbacks()))
                .toArray(ToolCallback[]::new);

        ChatClient.Builder b = builder
                .defaultSystem("""
                        Ты — полезный ассистент. Отвечай на языке пользователя.
                        У тебя есть инструменты для работы с базой объявлений о квартирах
                        (searchApartments, countApartments, getApartmentDetails, getStats).
                        ВСЕГДА используй их, когда вопрос касается конкретных объявлений,
                        цен, площадей, районов, сравнений — не выдумывай данные.
                        Если инструментов не хватило, честно скажи об этом.
                        Оформляй ответы в Markdown, используй таблицы для сравнений.
                        """)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .defaultOptions(OpenAiChatOptions.builder()
                        .temperature(0.2)
                        .reasoningEffort("none"));

        if (tools.length > 0) {
            b = b.defaultTools(tools);
        }

        this.chatMemory = chatMemory;
        this.chatClient = b.build();
    }

    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_PLAIN_VALUE)
    public Flux<String> stream(@RequestHeader("X-Session-Id") String sessionId,
                               @RequestBody String prompt) {
        return chatClient.prompt()
                .user(prompt)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, sessionId))
                .stream()
                .content()
                .onErrorResume(e -> Flux.just("\n\n⚠️ Ошибка соединения с моделью."));
    }

    /** История переписки для восстановления чата */
    @GetMapping("/chat/history/{sessionId}")
    public List<HistoryMessage> history(@PathVariable String sessionId) {
        return chatMemory.get(sessionId).stream()
                .filter(m -> m.getMessageType() == MessageType.USER
                        || m.getMessageType() == MessageType.ASSISTANT)
                .map(m -> new HistoryMessage(
                        m.getMessageType().name().toLowerCase(), m.getText()))
                .toList();

    }

    /** Очистка истории (кнопка «Очистить») */
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/chat/history/{sessionId}")
    public void clear(@PathVariable String sessionId) {
        chatMemory.clear(sessionId);
    }
}
