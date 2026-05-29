package my.help.useful.food.nutrients;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/nutrients")
@RequiredArgsConstructor
public class NutrientsController {

    private final NutrientsService nutrientsService;

    @GetMapping()
    public ResponseEntity<List<NutrientsModel>> getNutrientsList() {
        return ResponseEntity.ok(nutrientsService.getNutrientsList());
    }

    @GetMapping("{date}")
    public ResponseEntity<NutrientsModel> getNutrientsPerDate(@PathVariable LocalDate date) {
        return ResponseEntity.ok(nutrientsService.getNutrientsPerDate(date));
    }

    @PostMapping()
    public ResponseEntity<NutrientsModel> addNutrientsPerDay(@RequestBody NutrientsModel nutrientsModel) {
        return ResponseEntity.ok(nutrientsService.addNutrientsPerDay(nutrientsModel));
    }

    @PostMapping("/calculate")
    public ResponseEntity<NutrientsExpenditureRs> calculateDailyNutrients(@RequestBody NutrientsExpenditureRq request) {
        NutrientsExpenditureRs response = nutrientsService.calculateDailyNutrients(request);
        return ResponseEntity.ok(response);
    }
}
