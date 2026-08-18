package my.help.food.product;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import my.help.food.common.enums.Macronutrients;
import my.help.food.common.enums.Shop;
import my.help.food.product.dto.ProductRequest;
import my.help.food.product.dto.ProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping("/macronutrients")
    public ResponseEntity<Map<String, String>> getMacronutrients() {
        return ResponseEntity.ok(Arrays.stream(Macronutrients.values())
                .collect(Collectors.toMap(
                        Enum::name,
                        Macronutrients::getDisplayName
                )));
    }

    @GetMapping("/shops")
    public ResponseEntity<Map<String, String>> getShops() {
        return ResponseEntity.ok(Arrays.stream(Shop.values())
                .collect(Collectors.toMap(
                        Enum::name,
                        Shop::getDisplayName
                )));
    }

    @GetMapping
    public ResponseEntity<Page<ProductResponse>> getAllProducts(
            @RequestParam(value = "macronutrient", required = false) Macronutrients macronutrient,
            @RequestParam(value = "shop", required = false) Shop shop,
            @RequestParam(value = "name", required = false) String productName,
            @PageableDefault(size = 50, sort = "name") Pageable pageable) {
        return ResponseEntity.ok(productService.getAllProducts(macronutrient, shop, productName, pageable));
    }

    @PostMapping
    public ResponseEntity<ProductResponse> addProduct(@Valid @RequestBody ProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(productService.addProduct(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request) {
        return ResponseEntity.ok(productService.updateProduct(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}