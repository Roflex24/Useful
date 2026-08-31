package my.help.useful;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PageController {

    @GetMapping("/")
    public String index() {
        return "main";
    }

    @GetMapping("/page/vacancy")
    public String vacancyPage() {
        return "vacancy";
    }


    /*Food*/

    @GetMapping("/page/food")
    public String foodPage() {
        return "/food/food";
    }


    /*Planning*/

    @GetMapping("/page/kanban")
    public String kanbanPage(@RequestParam(required = false) Long id, Model model) {
        model.addAttribute("projectId", id);
        return "/planning/kanban";
    }

    @GetMapping("/page/projects")
    public String projectsPage() {
        return "/planning/projects";
    }

    @GetMapping("/page/planning")
    public String planningPage(Model model) {
        model.addAttribute("pageTitle", "Дашборд стратегического планирования");
        model.addAttribute("apiBaseUrl", "/api/planning");
        return "/planning/planning";
    }

    @GetMapping("/page/planning/{planId}/tasks")
    public String tasksPage(@PathVariable Long planId, Model model) {
        model.addAttribute("pageTitle", "Задачи плана");
        model.addAttribute("apiBaseUrl", "/api/planning");
        model.addAttribute("planId", planId);
        return "/planning/tasks";
    }

    @GetMapping("/page/ideas")
    public String ideasPage() {
        return "/planning/ideas";
    }


    /*Finance*/

    @GetMapping("/page/finance")
    public String financePage() {
        return "/finance/finance";
    }

    @GetMapping("/page/runway")
    public String runwayPage() {
        return "/finance/runway";
    }

    @GetMapping("/page/invest")
    public String investPage() {
        return "/finance/invest";
    }

    @GetMapping("/page/avito")
    public String avitoPage() {
        return "/finance/avito";
    }

    @GetMapping("/page/key/rate")
    public String keyratePage() {
        return "/finance/keyrate";
    }

    @GetMapping("/page/currency/rate")
    public String currencyRatePage() {
        return "/finance/currencyrate";
    }
}