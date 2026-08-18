package my.help.food.product;

import lombok.RequiredArgsConstructor;
import my.help.food.common.enums.Macronutrients;
import my.help.food.common.enums.Shop;
import my.help.food.common.exception.ProductNotFoundException;
import my.help.food.product.dto.ProductRq;
import my.help.food.product.dto.ProductRs;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Transactional(readOnly = true)
    public Page<ProductRs> getAllProducts(Macronutrients sortBy, Shop shop, String productName, Pageable pageable) {
        Specification<ProductEntity> spec = buildSpecification(shop, productName);
        Pageable sortedPageable = applySort(pageable, sortBy);

        Page<ProductEntity> page = productRepository.findAll(spec, sortedPageable);
        return page.map(productMapper::toResponse);
    }

    @Transactional
    public ProductRs addProduct(ProductRq request) {
        ProductEntity entity = productMapper.toEntity(request);
        ProductEntity saved = productRepository.save(entity);
        return productMapper.toResponse(saved);
    }

    @Transactional
    public ProductRs updateProduct(Long id, ProductRq request) {
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
    public Map<Long, ProductRs> getProductsByIds(Collection<Long> ids) {
        return productRepository.findAllById(ids).stream()
                .map(productMapper::toResponse)
                .collect(Collectors.toMap(ProductRs::id, Function.identity()));
    }

    private Specification<ProductEntity> buildSpecification(Shop shop, String productName) {
        Specification<ProductEntity> spec = (root, query, cb) -> cb.conjunction();

        if (shop != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("shop"), shop));
        }

        if (productName != null && !productName.isBlank()) {
            String pattern = productName.trim().toLowerCase() + "%";
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("name")), pattern));
        }

        return spec;
    }

    private Pageable applySort(Pageable pageable, Macronutrients sortBy) {
        if (sortBy == null) {
            return pageable;
        }
        String fieldName = sortBy.name().toLowerCase();
        Sort sort = Sort.by(Sort.Direction.DESC, fieldName)
                .and(Sort.by("name").ascending());
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
    }
}