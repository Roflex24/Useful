package my.help.food.nutrients;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/nutrients")
@RequiredArgsConstructor
public class NutrientsController {

    private final NutrientsService nutrientsService;

    @GetMapping
    public ResponseEntity<List<NutrientsModel>> getNutrientsList() {
        return ResponseEntity.ok(nutrientsService.getNutrientsList());
    }

    @GetMapping("/{date}")
    public ResponseEntity<NutrientsModel> getNutrientsPerDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(nutrientsService.getNutrientsPerDate(date));
    }

    @GetMapping("/week")
    public ResponseEntity<List<NutrientsModel>> getNutrientsPerDateForWeek() {
        return ResponseEntity.ok(nutrientsService.getNutrientsPerDateForWeek());
    }

    @PostMapping
    public ResponseEntity<NutrientsModel> addNutrientsPerDay(@RequestBody NutrientsModel nutrientsModel) {
        return ResponseEntity.status(HttpStatus.CREATED).body(nutrientsService.addNutrientsPerDay(nutrientsModel));
    }

    @PostMapping("/calculate")
    public ResponseEntity<NutrientsExpenditureRs> calculateDailyNutrients(@RequestBody NutrientsExpenditureRq request) {
        return ResponseEntity.ok(nutrientsService.calculateDailyNutrients(request));
    }
}