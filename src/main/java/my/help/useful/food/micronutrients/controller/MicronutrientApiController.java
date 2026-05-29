package my.help.useful.food.micronutrients.controller;

import my.help.useful.food.micronutrients.dto.ProductInfoRequestDto;
import my.help.useful.food.micronutrients.dto.ProductInfoResponseDto;
import my.help.useful.food.micronutrients.model.Micronutrient;
import my.help.useful.food.micronutrients.model.ProductInfo;
import my.help.useful.food.micronutrients.service.ProductInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class MicronutrientApiController {

    @Autowired
    private ProductInfoService productInfoService;

    // ==================== Эндпоинты для микронутриентов ====================

    @GetMapping("/micronutrients")
    public ResponseEntity<List<Micronutrient>> getAllMicronutrients() {
        return ResponseEntity.ok(productInfoService.getAllMicronutrients());
    }

    @GetMapping("/vitamins")
    public ResponseEntity<List<Micronutrient>> getVitamins() {
        return ResponseEntity.ok(productInfoService.getVitamins());
    }

    @GetMapping("/minerals")
    public ResponseEntity<List<Micronutrient>> getMinerals() {
        return ResponseEntity.ok(productInfoService.getMinerals());
    }

    @GetMapping("/fatty-acids")
    public ResponseEntity<List<Micronutrient>> getFattyAcids() {
        return ResponseEntity.ok(productInfoService.getFattyAcids());
    }

    @GetMapping("/water-soluble-vitamins")
    public ResponseEntity<List<Micronutrient>> getWaterSolubleVitamins() {
        return ResponseEntity.ok(productInfoService.getWaterSolubleVitamins());
    }

    @GetMapping("/fat-soluble-vitamins")
    public ResponseEntity<List<Micronutrient>> getFatSolubleVitamins() {
        return ResponseEntity.ok(productInfoService.getFatSolubleVitamins());
    }

    @GetMapping("/major-minerals")
    public ResponseEntity<List<Micronutrient>> getMajorMinerals() {
        return ResponseEntity.ok(productInfoService.getMajorMinerals());
    }

    @GetMapping("/trace-minerals")
    public ResponseEntity<List<Micronutrient>> getTraceMinerals() {
        return ResponseEntity.ok(productInfoService.getTraceMinerals());
    }

    @GetMapping("/micronutrients/{name}")
    public ResponseEntity<Micronutrient> getMicronutrientByName(@PathVariable String name) {
        return productInfoService.getMicronutrientByName(name)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ==================== Эндпоинты для продуктов ====================

    @GetMapping("/products")
    public ResponseEntity<List<ProductInfoResponseDto>> getAllProducts() {
        List<ProductInfoResponseDto> products = productInfoService.getAllProducts().stream()
                .map(ProductInfoResponseDto::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(products);
    }

    @GetMapping("/products/{id}")
    public ResponseEntity<ProductInfoResponseDto> getProductById(@PathVariable Long id) {
        return productInfoService.getProductById(id)
                .map(ProductInfoResponseDto::fromEntity)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/products/category/{category}")
    public ResponseEntity<List<ProductInfoResponseDto>> getProductsByCategory(@PathVariable String category) {
        List<ProductInfoResponseDto> products = productInfoService.getProductsByCategory(category).stream()
                .map(ProductInfoResponseDto::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(products);
    }

    @GetMapping("/products/search")
    public ResponseEntity<List<ProductInfoResponseDto>> searchProducts(@RequestParam String name) {
        List<ProductInfoResponseDto> products = productInfoService.searchProductsByName(name).stream()
                .map(ProductInfoResponseDto::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(products);
    }

    @PostMapping("/products")
    public ResponseEntity<ProductInfoResponseDto> createProduct(@RequestBody ProductInfoRequestDto request) {
        ProductInfo productInfo = new ProductInfo();
        productInfo.setName(request.getName());
        productInfo.setCategory(request.getCategory());
        productInfo.setMicronutrients(request.getMicronutrients() != null ? request.getMicronutrients() : new HashMap<>());

        ProductInfo savedProduct = productInfoService.saveProduct(productInfo);
        return ResponseEntity.status(HttpStatus.CREATED).body(ProductInfoResponseDto.fromEntity(savedProduct));
    }

    @PutMapping("/products/{id}")
    public ResponseEntity<ProductInfoResponseDto> updateProduct(@PathVariable Long id,
                                                                @RequestBody ProductInfoRequestDto request) {
        return productInfoService.getProductById(id)
                .map(existingProduct -> {
                    existingProduct.setName(request.getName());
                    existingProduct.setCategory(request.getCategory());
                    existingProduct.setMicronutrients(request.getMicronutrients() != null ? request.getMicronutrients() : new HashMap<>());
                    ProductInfo savedProduct = productInfoService.saveProduct(existingProduct);
                    return ResponseEntity.ok(ProductInfoResponseDto.fromEntity(savedProduct));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/products/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        if (productInfoService.getProductById(id).isPresent()) {
            productInfoService.deleteProduct(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    // ==================== Эндпоинты для топ-продуктов ====================

    @GetMapping("/top-products")
    public ResponseEntity<Map<String, List<Map<String, Object>>>> getTopProducts() {
        return ResponseEntity.ok(productInfoService.getTopProductsByMicronutrient());
    }

    @GetMapping("/top-products/{micronutrientName}")
    public ResponseEntity<Map<String, List<Map<String, Object>>>> getTopProductsForMicronutrient(
            @PathVariable String micronutrientName) {
        return ResponseEntity.ok(productInfoService.getTopProductsForMicronutrient(micronutrientName));
    }
}