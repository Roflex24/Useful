package my.help.food.product;

import lombok.RequiredArgsConstructor;
import my.help.food.product.dto.ProductRequest;
import my.help.food.product.dto.ProductResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Transactional(readOnly = true)
    public List<ProductResponse> getAllProducts(Macronutrients sortBy, Shop shop, String productName) {
        // Пока оставим как было, но позже перейдём на Specification и Pageable
        List<ProductEntity> productEntityList;

        if (productName == null) {
            productEntityList = productRepository.findAll();
        } else {
            productEntityList = productRepository.findByNameStartingWithIgnoreCase(productName);
        }

        return productMapper.toResponseList(productEntityList.stream()
                .filter(e -> shop == null || e.getShop() == shop)
                .sorted(createComparator(sortBy))
                .toList());
    }

    @Transactional
    public ProductResponse addProduct(ProductRequest request) {
        ProductEntity entity = productMapper.toEntity(request);
        ProductEntity saved = productRepository.save(entity);
        return productMapper.toResponse(saved);
    }

    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        ProductEntity productEntity = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
        productMapper.updateEntityFromRequest(request, productEntity);
        ProductEntity updated = productRepository.save(productEntity);
        return productMapper.toResponse(updated);
    }

    @Transactional
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ProductNotFoundException(id);
        }
        productRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {
        return productMapper.toResponse(productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id)));
    }

    @Transactional(readOnly = true)
    public Map<Long, ProductResponse> getProductsByIds(Collection<Long> ids) {
        return productRepository.findAllById(ids).stream()
                .map(productMapper::toResponse)
                .collect(Collectors.toMap(ProductResponse::id, Function.identity()));
    }

    private Comparator<ProductEntity> createComparator(Macronutrients macronutrient) {
        Comparator<ProductEntity> comparator = Comparator.comparing(ProductEntity::getName);

        if (macronutrient != null) {
            Comparator<ProductEntity> macroComparator = Comparator.comparing(e ->
                    switch (macronutrient) {
                        case CALORIES -> e.getCalories();
                        case PROTEIN -> e.getProtein();
                        case FAT -> e.getFat();
                        case CARBOHYDRATE -> e.getCarbohydrate();
                        case FIBER -> e.getFiber();
                    }
            );
            comparator = macroComparator.reversed().thenComparing(ProductEntity::getName);
        }

        return comparator;
    }
}