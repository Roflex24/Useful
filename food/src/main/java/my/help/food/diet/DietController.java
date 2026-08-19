package my.help.food.diet;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import my.help.food.diet.dto.DietRq;
import my.help.food.diet.dto.DietRs;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/diets")
@RequiredArgsConstructor
@Tag(name = "Food API", description = "Продукты и питание")
public class DietController {

    private final DietService dietService;

    @GetMapping
    public List<DietRs> getList() {
        return dietService.getList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DietRs create(@Valid @RequestBody DietRq rq) {
        return dietService.create(rq);
    }

    @PutMapping("/{id}")
    public DietRs update(
            @PathVariable @Positive Long id,
            @Valid @RequestBody DietRq rq) {
        return dietService.update(id, rq);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        dietService.delete(id);
    }
}