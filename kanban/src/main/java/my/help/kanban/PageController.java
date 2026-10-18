package my.help.kanban;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PageController {

    @GetMapping("/")
    public String index() {
        return "redirect:/page/projects";
    }

    @GetMapping("/page/kanban")
    public String kanbanPage(@RequestParam(required = false) Long id, Model model) {
        model.addAttribute("projectId", id);
        return "planning/kanban";
    }

    @GetMapping("/page/projects")
    public String projectsPage() {
        return "planning/projects";
    }

    @GetMapping("/page/planning")
    public String planningPage(Model model) {
        model.addAttribute("pageTitle", "Дашборд стратегического планирования");
        model.addAttribute("apiBaseUrl", "/api/planning");
        return "planning/planning";
    }

    @GetMapping("/page/planning/{planId}/tasks")
    public String tasksPage(@PathVariable Long planId, Model model) {
        model.addAttribute("pageTitle", "Задачи плана");
        model.addAttribute("apiBaseUrl", "/api/planning");
        model.addAttribute("planId", planId);
        return "planning/tasks";
    }

    @GetMapping("/page/ideas")
    public String ideasPage() {
        return "planning/ideas";
    }
}
