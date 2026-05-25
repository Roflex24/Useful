package my.help.useful.deadlock;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Random;

@Slf4j
@RestController
public class DeadlockController {

    @GetMapping("/deadlock")
    public ResponseEntity<DeadlockModel> getDeadlockDecision() {
        Random random = new Random();
        int chance = random.nextInt(100);

        // 10% шанс получить ДА, 90% шанс получить НЕТ
        if(chance < 10) {
            log.info("Можно играть в Deadlock");
            String[] messages = {
                    "🎉 Да! Сегодня можно играть в Deadlock!",
                    "✅ Удача на твоей стороне! Запускай игру!",
                    "🎮 Игровой день! Deadlock ждет тебя!",
                    "🔥 Отличный шанс поиграть! Удачи в матчах!"
            };
            return ResponseEntity.ok(
                    new DeadlockModel(true, messages[random.nextInt(messages.length)]));
        } else {
            log.info("Нельзя играть в Deadlock");
            String[] messages = {
                    "⏰ Сегодня лучше заняться делами, Deadlock подождет",
                    "📚 Лучше удели время учебе или работе",
                    "💤 Отдохни сегодня, завтра будет новый шанс",
                    "🏃 Заняться спортом будет полезнее, чем Deadlock",
                    "📖 Почитай книгу или посмотри фильм"
            };
            return ResponseEntity.ok(
                    new DeadlockModel(false, messages[random.nextInt(messages.length)]));
        }
    }
}