package my.help.food.nutrients;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import my.help.food.nutrients.dto.NutrientsRequest;
import my.help.food.nutrients.dto.NutrientsResponse;
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
    public ResponseEntity<List<NutrientsResponse>> getNutrientsList() {
        return ResponseEntity.ok(nutrientsService.getNutrientsList());
    }

    @GetMapping("/week")
    public ResponseEntity<List<NutrientsResponse>> getNutrientsPerDateForWeek() {
        return ResponseEntity.ok(nutrientsService.getNutrientsPerDateForWeek());
    }

    @GetMapping("/{date}")
    public ResponseEntity<NutrientsResponse> getNutrientsPerDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(nutrientsService.getNutrientsPerDate(date));
    }

    @PostMapping
    public ResponseEntity<NutrientsResponse> addNutrientsPerDay(
            @Valid @RequestBody NutrientsRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(nutrientsService.addNutrientsPerDay(request));
    }

    @PostMapping("/calculate")
    public ResponseEntity<NutrientsExpenditureRs> calculateDailyNutrients(
            @Valid @RequestBody NutrientsExpenditureRq request) {
        return ResponseEntity.ok(nutrientsService.calculateDailyNutrients(request));
    }
}