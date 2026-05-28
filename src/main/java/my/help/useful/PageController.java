package my.help.useful;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PageController {

    private static final Logger logger = LoggerFactory.getLogger(PageController.class);

    @GetMapping("/")
    public String index(Model model) {
        logger.info("Открыта главная страница");
        return "index";
    }

    @GetMapping("/page/food")
    public String foodPage(Model model) {
        logger.info("Открыта страница вкусняшек");
        return "food";
    }

    @GetMapping("/page/deadlock")
    public String deadlockPage(Model model) {
        logger.info("Открыта страница Deadlock");
        return "deadlock";
    }

    @GetMapping("/page/weather")
    public String weatherPage(Model model) {
        logger.info("Открыта страница с погодой");
        return "weather";
    }

    @GetMapping("/page/key/rate")
    public String keyratePage(Model model) {
        logger.info("Открыта страница с ключевой ставкой");
        return "keyrate";
    }

    @GetMapping("/page/currency/rate")
    public String currencyRatePage(Model model) {
        logger.info("Открыта страница с курсами валют");
        return "currencyrate";
    }

    @GetMapping("/page/vacancy")
    public String vacancyPage(Model model) {
        logger.info("Открыта страница с вакансиями");
        return "vacancy";
    }

    @GetMapping("/page/calculate/calories")
    public String productPage(Model model) {
        logger.info("Открыта страница с продуктами");
        return "calculate_calories";
    }

    @GetMapping("/page/kanban")
    public String kanbanPage(@RequestParam(required = false) Long id, Model model) {
        logger.info("Открыта страница с канбан доской для проекта ID: {}", id);
        model.addAttribute("projectId", id);
        return "kanban";
    }

    @GetMapping("/page/projects")
    public String projectsPage(Model model) {
        logger.info("Открыта страница с проектами");
        return "projects";
    }

    @GetMapping("/page/planning")
    public String planningPage(Model model) {
        model.addAttribute("pageTitle", "Дашборд стратегического планирования");
        model.addAttribute("apiBaseUrl", "/api/planning");
        return "planning";
    }

    @GetMapping("/page/planning/{planId}/tasks")
    public String tasksPage(@PathVariable Long planId, Model model) {
        model.addAttribute("pageTitle", "Задачи плана");
        model.addAttribute("apiBaseUrl", "/api/planning");
        model.addAttribute("planId", planId);
        return "tasks";
    }
}