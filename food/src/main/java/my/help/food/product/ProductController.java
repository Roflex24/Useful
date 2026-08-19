package my.help.food.product;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import my.help.food.common.enums.Macronutrients;
import my.help.food.common.enums.Shop;
import my.help.food.product.dto.ProductRq;
import my.help.food.product.dto.ProductRs;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Food API", description = "Продукты и питание")
public class ProductController {

    private final ProductService productService;

    @GetMapping("/macronutrients")
    public Map<String, String> getMacronutrients() {
        return Arrays.stream(Macronutrients.values())
                .collect(Collectors.toMap(
                        Enum::name,
                        Macronutrients::getDisplayName
                ));
    }

    @GetMapping("/shops")
    public Map<String, String> getShops() {
        return Arrays.stream(Shop.values())
                .collect(Collectors.toMap(
                        Enum::name,
                        Shop::getDisplayName
                ));
    }

    @GetMapping
    public Page<ProductRs> search(
            @RequestParam(value = "macronutrient", required = false) Macronutrients macronutrient,
            @RequestParam(value = "shop", required = false) Shop shop,
            @RequestParam(value = "name", required = false) String name,
            @PageableDefault(size = 50, sort = "name") Pageable pageable) {
        return productService.search(macronutrient, shop, name, pageable);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductRs create(@Valid @RequestBody ProductRq rq) {
        return productService.create(rq);
    }

    @PutMapping("/{id}")
    public ProductRs update(
            @PathVariable Long id,
            @Valid @RequestBody ProductRq rq) {
        return productService.update(id, rq);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        productService.delete(id);
    }
}