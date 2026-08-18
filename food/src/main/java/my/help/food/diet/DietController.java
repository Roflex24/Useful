package my.help.food.diet;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/diet")
@RequiredArgsConstructor
public class DietController {

    private final DietService dietService;

    @GetMapping
    public ResponseEntity<List<DietModel>> getAllDiets() {
        return ResponseEntity.ok(dietService.getAllDiets());
    }

    @PostMapping
    public ResponseEntity<DietModel> createDiet(@RequestBody DietModel dietModel) {
        return ResponseEntity.status(HttpStatus.CREATED).body(dietService.createDiet(dietModel));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DietModel> updateDiet(@PathVariable Long id, @RequestBody DietModel dietModel) {
        dietModel.setId(id);
        return ResponseEntity.ok(dietService.updateDiet(dietModel));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDiet(@PathVariable Long id) {
        dietService.deleteDiet(id);
        return ResponseEntity.noContent().build();
    }
}
