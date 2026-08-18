package my.help.food.product;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Transactional(readOnly = true)
    public List<ProductModel> getAllProducts(Macronutrients macronutrient, Shop shop, String productName) {
        List<ProductEntity> productEntityList;

        if (productName == null) {
            productEntityList = productRepository.findAll();
        } else {
            productEntityList = productRepository.findByNameStartingWithIgnoreCase(productName);
        }

        return productMapper.toModelList(productEntityList.stream()
                .filter(e -> shop == null || Objects.equals(e.getShop(), shop))
                .sorted(createComparator(macronutrient))
                .toList());
    }

    @Transactional
    public ProductModel addProduct(ProductModel productModel) {
        ProductEntity entity = productMapper.toEntity(productModel);
        ProductEntity saved = productRepository.save(entity);
        return productMapper.toModel(saved);
    }

    @Transactional
    public ProductModel updateProduct(ProductModel productModel) {
        ProductEntity productEntity = productRepository.findById(productModel.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
        productMapper.updateEntityFromModel(productModel, productEntity);
        ProductEntity updated = productRepository.save(productEntity);
        return productMapper.toModel(updated);
    }

    @Transactional
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found");
        }
        productRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public ProductModel getProductById(Long id) {
        return productMapper.toModel(productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found")));
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