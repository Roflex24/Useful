package my.help.food.nutrients;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.help.food.nutrients.dto.NutrientsRs;
import my.help.food.nutrients.dto.NutrientsUpdateRq;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/nutrients")
@RequiredArgsConstructor
@Tag(name = "Food API", description = "Продукты и питание")
public class NutrientsController {

    private final NutrientsService nutrientsService;

    @GetMapping
    public List<NutrientsRs> getList() {
        log.info("Запрос на получение списка всех дней питания");
        return nutrientsService.getList();
    }

    @GetMapping("/week")
    public List<NutrientsRs> getForWeek() {
        log.info("Запрос на получение данных за неделю");
        return nutrientsService.getForWeek();
    }

    @GetMapping("/{date}")
    public NutrientsRs getPerDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        log.info("Запрос данных за дату {}", date);
        return nutrientsService.getPerDate(date);
    }

    @PutMapping("/{date}")
    public NutrientsRs updatePerDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @Valid @RequestBody NutrientsUpdateRq rq) {
        log.info("Обновление данных за дату {}, продуктов: {}", date,
                rq.productsPerDay() != null ? rq.productsPerDay().size() : 0);
        return nutrientsService.updatePerDate(date, rq);
    }
}