package my.help.food.product;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final ProductValidator productValidator;

    @Transactional(readOnly = true)
    public Page<ProductRs> search(Macronutrients sortByMacronutrient, Shop shop, String name, Pageable pageable) {
        log.debug("Построение спецификации для поиска продуктов");
        Specification<ProductEntity> spec = buildSpecification(shop, name);
        Pageable sortedPageable = applySort(pageable, sortByMacronutrient);

        Page<ProductEntity> page = productRepository.findAll(spec, sortedPageable);
        log.info("Найдено продуктов: {} (всего: {})", page.getNumberOfElements(), page.getTotalElements());
        return page.map(productMapper::toResponse);
    }

    @Transactional
    public ProductRs create(ProductRq rq) {
        log.info("Создание продукта: name={}", rq.name());
        ProductEntity entity = productMapper.toEntity(rq);
        ProductEntity saved = productRepository.save(entity);
        log.info("Продукт создан с id={}", saved.getId());
        return productMapper.toResponse(saved);
    }

    @Transactional
    public ProductRs update(Long id, ProductRq rq) {
        log.info("Обновление продукта id={}", id);
        ProductEntity productEntity = productRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Продукт с id={} не найден", id);
                    return new ProductNotFoundException(id);
                });
        productMapper.updateEntityFromRequest(rq, productEntity);
        ProductEntity updated = productRepository.save(productEntity);
        log.info("Продукт id={} успешно обновлён", id);
        return productMapper.toResponse(updated);
    }

    @Transactional
    public void delete(Long id) {
        log.info("Удаление продукта id={}", id);
        if (!productRepository.existsById(id)) {
            log.warn("Попытка удаления несуществующего продукта id={}", id);
            throw new ProductNotFoundException(id);
        }
        productValidator.validateDeletable(id);
        productRepository.deleteById(id);
        log.info("Продукт id={} удалён", id);
    }

    private Specification<ProductEntity> buildSpecification(Shop shop, String name) {
        Specification<ProductEntity> spec = (root, query, cb) -> cb.conjunction();

        if (shop != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("shop"), shop));
        }

        if (name != null && !name.isBlank()) {
            String pattern = "%" + name.trim().toLowerCase() + "%";
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("name")), pattern));
        }

        return spec;
    }

    private Pageable applySort(Pageable pageable, Macronutrients sortByMacronutrient) {
        if (sortByMacronutrient == null) {
            return pageable;
        }
        String fieldName = sortByMacronutrient.name().toLowerCase();
        Sort sort = Sort.by(Sort.Direction.DESC, fieldName)
                .and(Sort.by("name").ascending());
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
    }
}
