package my.help.useful.calculate_calories.product;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

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

    public void addProduct(ProductModel productModel) {
        productRepository.save(productMapper.toEntity(productModel));
    }

    public void updateProduct(ProductModel productModel) {
        Optional<ProductEntity> productEntityOptional = productRepository.findById(productModel.getId());
        if (productEntityOptional.isPresent()) {
            ProductEntity productEntity = productEntityOptional.get();
            productMapper.updateEntityFromModel(productModel, productEntity);
            productRepository.save(productEntity);
        } else {
            throw new RuntimeException("Product not found");
        }
    }

    @Transactional
    public void deleteProduct(ProductModel productModel) {
        productRepository.delete(productMapper.toEntity(productModel));
    }

    public ProductModel getProductById(Long id) {
        Optional<ProductEntity> productEntityOptional = productRepository.findById(id);
        if (productEntityOptional.isPresent()) {
            return productMapper.toModel(productEntityOptional.get());
        }
        throw new RuntimeException("Product not found");
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
