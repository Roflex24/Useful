package my.help.food.diet;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import my.help.food.diet.dto.DietRq;
import my.help.food.diet.dto.DietRs;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/diets")
@RequiredArgsConstructor
@Tag(name = "Food API", description = "Продукты и питание")
public class DietController {

    private final DietService dietService;

    @GetMapping
    public ResponseEntity<List<DietRs>> getList() {
        return ResponseEntity.ok(dietService.getList());
    }

    @PostMapping
    public ResponseEntity<DietRs> create(@Valid @RequestBody DietRq rq) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(dietService.create(rq));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DietRs> update(
            @PathVariable Long id,
            @Valid @RequestBody DietRq rq) {
        return ResponseEntity.ok(dietService.update(id, rq));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        dietService.delete(id);
        return ResponseEntity.noContent().build();
    }
}