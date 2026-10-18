package my.help.useful;

import org.springframework.stereotype.Controller;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {
    @Value("${services.finance.url}")
    private String financeUrl;
    @Value("${services.food.url}")
    private String foodUrl;
    @Value("${services.kanban.url}")
    private String kanbanUrl;

    @GetMapping("/page/finance")
    public String finance() { return "redirect:" + financeUrl + "/page/finance"; }
    @GetMapping("/page/food")
    public String food() { return "redirect:" + foodUrl + "/page/food"; }
    @GetMapping("/page/projects")
    public String projects() { return "redirect:" + kanbanUrl + "/page/projects"; }
    @GetMapping("/page/ai/chat")
    public String chat() { return "redirect:" + financeUrl + "/page/ai/chat"; }


    @GetMapping("/")
    public String index() {
        return "main";
    }

    @GetMapping("/page/vacancy")
    public String vacancyPage() {
        return "vacancy";
    }
}
