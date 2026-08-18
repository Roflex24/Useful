package my.help.food.diet;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import my.help.food.diet.dto.DietRequest;
import my.help.food.diet.dto.DietResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/diets")  // изменено на /diets
@RequiredArgsConstructor
public class DietController {

    private final DietService dietService;

    @GetMapping
    public ResponseEntity<List<DietResponse>> getAllDiets() {
        return ResponseEntity.ok(dietService.getAllDiets());
    }

    @PostMapping
    public ResponseEntity<DietResponse> createDiet(@Valid @RequestBody DietRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(dietService.createDiet(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DietResponse> updateDiet(
            @PathVariable Long id,
            @Valid @RequestBody DietRequest request) {
        return ResponseEntity.ok(dietService.updateDiet(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDiet(@PathVariable Long id) {
        dietService.deleteDiet(id);
        return ResponseEntity.noContent().build();
    }
}