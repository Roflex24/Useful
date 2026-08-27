package my.help.useful.motivate;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.help.useful.redis.RedisService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Random;

@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "Common API", description = "Общее")
public class MotivateController {

    private final RedisService redisService;

    @GetMapping("/motivate")
    public ResponseEntity<MotivateModel> getMainInformation() {

        Random random = new Random();

        log.info("Отдаю мотивационную фразу");
        return ResponseEntity.ok(new MotivateModel(redisService.getValue(random.nextInt(redisService.getTotalKeysCount()))));
    }
}
