package my.help.useful.calculate_calories.product;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/product")
public class ProductController {

    private final ProductService productService;

    @GetMapping("/macronutrients")
    public ResponseEntity<Map<String, String>> getMacronutrients() {
        return ResponseEntity.ok(Arrays.stream(Macronutrients.values())
                .collect(Collectors.toMap(
                        Enum::name,           // ключ: "PROTEIN"
                        Macronutrients::getDisplayName  // значение: "Белки"
                )));
    }

    @GetMapping("/shop")
    public ResponseEntity<Map<String, String>> getShop() {
        return ResponseEntity.ok(Arrays.stream(Shop.values())
                .collect(Collectors.toMap(
                        Enum::name,
                        Shop::getDisplayName
                )));
    }

    @GetMapping("/all")
    public ResponseEntity<List<ProductModel>> getAllProducts(
            @RequestParam(value = "macronutrient", required = false) Macronutrients macronutrient,
            @RequestParam(value = "shop", required = false) Shop shop,
            @RequestParam(value = "name", required = false) String productName) {
        return ResponseEntity.ok(productService.getAllProducts(macronutrient, shop, productName));
    }

    @PostMapping()
    public ResponseEntity<ProductModel> addProduct(@RequestBody ProductModel productModel) {
        productService.addProduct(productModel);
        return ResponseEntity.ok(productModel);
    }

    @PostMapping("/update")
    public ResponseEntity<ProductModel> updateProduct(@RequestBody ProductModel productModel) {
        productService.updateProduct(productModel);
        return ResponseEntity.ok(productModel);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<ProductModel> deleteProduct(@RequestBody ProductModel productModel) {
        productService.deleteProduct(productModel);
        return ResponseEntity.ok(productModel);
    }
}
