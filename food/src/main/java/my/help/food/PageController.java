package my.help.food;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/")
    public String index() {
        return "redirect:/page/food";
    }

    @GetMapping("/page/food")
    public String foodPage() {
        return "food/food";
    }
}
