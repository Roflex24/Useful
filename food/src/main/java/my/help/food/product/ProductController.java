package my.help.food.product;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
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
    public ResponseEntity<List<ProductModel>> getAllProducts(
            @RequestParam(value = "macronutrient", required = false) Macronutrients macronutrient,
            @RequestParam(value = "shop", required = false) Shop shop,
            @RequestParam(value = "name", required = false) String productName) {
        return ResponseEntity.ok(productService.getAllProducts(macronutrient, shop, productName));
    }

    @PostMapping
    public ResponseEntity<ProductModel> addProduct(@RequestBody ProductModel productModel) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.addProduct(productModel));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductModel> updateProduct(@PathVariable Long id, @RequestBody ProductModel productModel) {
        productModel.setId(id);
        return ResponseEntity.ok(productService.updateProduct(productModel));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}