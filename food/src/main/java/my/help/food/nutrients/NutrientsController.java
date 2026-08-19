package my.help.food.nutrients;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import my.help.food.nutrients.dto.NutrientsRs;
import my.help.food.nutrients.dto.NutrientsUpdateRq;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<List<NutrientsRs>> getList() {
        return ResponseEntity.ok(nutrientsService.getList());
    }

    @GetMapping("/week")
    public ResponseEntity<List<NutrientsRs>> getForWeek() {
        return ResponseEntity.ok(nutrientsService.getForWeek());
    }

    @GetMapping("/{date}")
    public ResponseEntity<NutrientsRs> getPerDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(nutrientsService.getPerDate(date));
    }

    @PutMapping("/{date}")
    public ResponseEntity<NutrientsRs> updatePerDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @Valid @RequestBody NutrientsUpdateRq rq) {
        return ResponseEntity.ok(nutrientsService.updatePerDate(date, rq));
    }
}