package my.help.food.diet;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/diet")
public class DietController {

    private final DietService dietService;

    @GetMapping()
    public ResponseEntity<List<DietModel>> getAllDiets() {
        return ResponseEntity.ok(dietService.getAllDiets());
    }

    @PostMapping()
    public ResponseEntity<DietModel> createDiet(@RequestBody DietModel dietModel) {
        return ResponseEntity.ok(dietService.createDiet(dietModel));
    }

    @PostMapping("/update")
    public ResponseEntity<DietModel> updateDiet(@RequestBody DietModel dietModel) {
        return ResponseEntity.ok(dietService.updateDiet(dietModel));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteDiet(@PathVariable Long id) {
        dietService.deleteDiet(id);
        return ResponseEntity.ok().build();
    }
}
