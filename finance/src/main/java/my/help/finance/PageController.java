package my.help.finance;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/")
    public String index() {
        return "redirect:/page/finance";
    }

    @GetMapping("/page/finance")
    public String financePage() {
        return "finance/finance";
    }

    @GetMapping("/page/analysis")
    public String analysisPage() {
        return "finance/analysis";
    }

    @GetMapping("/page/runway")
    public String runwayPage() {
        return "finance/runway";
    }

    @GetMapping("/page/invest")
    public String investPage() {
        return "finance/invest";
    }

    @GetMapping("/page/ofz")
    public String ofzPage() {
        return "finance/ofz";
    }

    @GetMapping("/page/deposits")
    public String depositsPage() {
        return "finance/deposits";
    }

    @GetMapping("/page/avito")
    public String avitoPage() {
        return "finance/avito";
    }

    @GetMapping("/page/key/rate")
    public String keyratePage() {
        return "finance/keyrate";
    }

    @GetMapping("/page/currency/rate")
    public String currencyRatePage() {
        return "finance/currencyrate";
    }

    @GetMapping("/page/ai/chat")
    public String aiChatPage() {
        return "chat";
    }
}
