package my.help.useful.ai;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/ai")
public class AiController {

    private final GigaChatService aiService;

    @GetMapping("/ask")
    public String ask(@RequestParam String prompt) {
        return aiService.ask(prompt);
    }
}