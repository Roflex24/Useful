package my.help.useful.ai;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import my.help.useful.calculate_calories.nutrients.NutrientsModel;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class LlamaService {

    private final Gson gson = new GsonBuilder().create();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    static class LlamaResponse {
        String response;
        boolean done;
    }

    // Метод принимает строку с JSON данными
    public String analyzeDiet(String jsonData) {
        try {
            String prompt = buildPrompt(jsonData);

            String jsonRequest = String.format("""
                {
                    "model": "llama3.1:8b",
                    "prompt": "%s",
                    "stream": false,
                    "options": {
                        "temperature": 0.1,
                        "num_predict": 500
                    }
                }
                """, escapeJson(prompt));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:11434/api/generate"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonRequest))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                LlamaResponse llamaResponse = gson.fromJson(response.body(), LlamaResponse.class);
                return llamaResponse.response;
            } else {
                return "Ошибка от Ollama: " + response.statusCode();
            }

        } catch (Exception e) {
            return "Ошибка анализа: " + e.getMessage();
        }
    }

    private String buildPrompt(String jsonData) {
        return """
        Ты - эксперт-диетолог с 20-летним опытом. Проанализируй рацион и дай точную оценку.
        
        ВАЖНЫЕ ПРАВИЛА:
        1. НЕ выдумывай продукты, которых нет в списке
        2. Опирайся ТОЛЬКО на предоставленные данные
        3. Если данных недостаточно - так и напиши
        4. Используй научно-обоснованные нормы потребления витаминов
        
        Анализируй по шагам:
        Шаг 1: Посчитай реальное потребление по каждому продукту (quantity * на 100г/шт)
        Шаг 2: Определи основные источники витаминов из продуктов:
           - Витамин C: цитрусовые, ягоды, капуста
           - Витамин A: морковь, тыква, яйца
           - Витамины B: мясо, яйца, молочка
           - Витамин D: рыба, яйца, молочка
           - Кальций: молочка, зелень
           - Железо: мясо, гречка, яблоки
           - Магний: орехи, зелень, каши
           - Калий: бананы, картофель, зелень
        
        Шаг 3: Сравни с суточными нормами:
           - Белок: 1.2-1.5г на кг веса (для вашего рациона ~80-120г)
           - Жиры: 0.8-1г на кг веса (~50-70г)
           - Углеводы: 3-5г на кг веса (~200-300г)
           - Клетчатка: 25-30г
        
        Формат ответа (строго):
        • Витамины и минералы: [есть/нет конкретные элементы]
        • Дефицит: [список отсутствующих элементов]
        • БЖУ: [анализ белков/жиров/углеводов]
        • Рекомендации: [2-3 конкретных совета по продуктам]
        
        Данные: %s
        """.formatted(jsonData);
    }

    private String escapeJson(String text) {
        return text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}