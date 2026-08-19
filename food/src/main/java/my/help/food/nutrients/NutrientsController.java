package my.help.food.nutrients;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import my.help.food.nutrients.dto.NutrientsRs;
import my.help.food.nutrients.dto.NutrientsUpdateRq;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/nutrients")
@RequiredArgsConstructor
@Tag(name = "Food API", description = "Продукты и питание")
public class NutrientsController {

    private final NutrientsService nutrientsService;

    @GetMapping
    public List<NutrientsRs> getList() {
        return nutrientsService.getList();
    }

    @GetMapping("/week")
    public List<NutrientsRs> getForWeek() {
        return nutrientsService.getForWeek();
    }

    @GetMapping("/{date}")
    public NutrientsRs getPerDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return nutrientsService.getPerDate(date);
    }

    @PutMapping("/{date}")
    public NutrientsRs updatePerDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @Valid @RequestBody NutrientsUpdateRq rq) {
        return nutrientsService.updatePerDate(date, rq);
    }
}