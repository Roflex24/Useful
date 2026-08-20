package my.help.food.product;

import lombok.RequiredArgsConstructor;
import my.help.food.common.exception.ProductInUseException;
import my.help.food.diet.DietRepository;
import my.help.food.products_per_day.ProductsPerDayRepository;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class ProductValidator {

    private final ProductRepository productRepository;
    private final ProductsPerDayRepository productsPerDayRepository;
    private final DietRepository dietRepository;

    public void validateExist(Collection<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return;
        }
        Set<Long> uniqueIds = productIds.stream()
                .filter(Objects::nonNull)
                .collect(java.util.stream.Collectors.toSet());
        if (productRepository.countByIdIn(uniqueIds) != uniqueIds.size()) {
            throw new IllegalArgumentException("Один или несколько продуктов не найдены");
        }
    }

    public void validateNoDuplicateProductIds(Collection<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return;
        }
        Set<Long> seen = new HashSet<>();
        for (Long productId : productIds) {
            if (!seen.add(productId)) {
                throw new IllegalArgumentException("Дубликат productId: " + productId);
            }
        }
    }

    public void validateDeletable(Long productId) {
        if (productsPerDayRepository.existsById_ProductId(productId)
                || dietRepository.existsByItemsProductId(productId)) {
            throw new ProductInUseException(productId);
        }
    }
}
