package my.help.useful.food;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Random;

@Slf4j
@RestController
public class FoodController {

    @GetMapping("/snack")
    public ResponseEntity<FoodModel> getSnack() {
        Random random = new Random();
        int i = random.nextInt(100);

        if(i < 25) {
            log.info("Можно вкусняшки");
            return ResponseEntity.ok(
                    new FoodModel(true, "Сегодня можно погрызть вкусняшки кайфуем"));
        } else {
            log.info("Нельзя вкусняшки");
            return ResponseEntity.ok(
                    new FoodModel(false, "Прости братишка, но сегодня пп иди поешь фруктов"));
        }
    }
}
