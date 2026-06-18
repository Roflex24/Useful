package my.help.useful.ai;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class GigaChatService {

    private final RestClient restClient;


    public GigaChatService(RestClient restClient) {
        this.restClient = restClient;
    }

    public String ask(String prompt) {

        String token = getAccessToken();

        Map<String, Object> request = Map.of(
                "model", "GigaChat",
                "temperature", 0.2,
                "max_tokens", 60,
                "messages", List.of(
                        Map.of(
                                "role", "system",
                                "content",
                                """
                                Ты полезный AI-ассистент.
                                Отвечай только одним коротким предложением.
                                Максимум 20 слов.
                                Без списков и лишних объяснений.
                                """
                        ),
                        Map.of(
                                "role", "user",
                                "content", prompt
                        )
                )
        );

        return restClient.post()
                .uri("https://gigachat.devices.sberbank.ru/api/v1/chat/completions")
                .header("Authorization", "Bearer " + token)
                .body(request)
                .retrieve()
                .body(String.class);
    }

    private String getAccessToken() {

        String rqUid = UUID.randomUUID().toString();

        String apiKey = "MDE5ZWNhZTktNzQzZC03NDhkLWJjYzctMTQ2MDk4OGJmZmU0OmYzZWU5Y2FjLWY0YzctNDBiOS1iZDkzLWEzMjJhODZiZDMyMQ==";
        return restClient.post()
                .uri("https://ngw.devices.sberbank.ru:9443/api/v2/oauth")
                .header("Authorization", "Basic " + apiKey)
                .header("RqUID", rqUid)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .body("scope=GIGACHAT_API_PERS")
                .retrieve()
                .body(TokenResponse.class)
                .accessToken();
    }
}


