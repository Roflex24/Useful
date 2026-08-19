package my.help.food.diet;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.help.food.diet.dto.DietRq;
import my.help.food.diet.dto.DietRs;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/diets")
@RequiredArgsConstructor
@Tag(name = "Food API", description = "Продукты и питание")
public class DietController {

    private final DietService dietService;

    @GetMapping
    public List<DietRs> getList() {
        log.info("Запрос на получение списка рационов");
        return dietService.getList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DietRs create(@Valid @RequestBody DietRq rq) {
        log.info("Запрос на создание рациона: name={}, itemsCount={}", rq.name(), rq.items() != null ? rq.items().size() : 0);
        return dietService.create(rq);
    }

    @PutMapping("/{id}")
    public DietRs update(
            @PathVariable @Positive Long id,
            @Valid @RequestBody DietRq rq) {
        log.info("Запрос на обновление рациона id={}, name={}, itemsCount={}", id, rq.name(), rq.items() != null ? rq.items().size() : 0);
        return dietService.update(id, rq);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        log.info("Запрос на удаление рациона id={}", id);
        dietService.delete(id);
    }
}