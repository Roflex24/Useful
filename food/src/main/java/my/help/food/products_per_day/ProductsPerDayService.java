package my.help.food.products_per_day;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductsPerDayService {

    private final ProductsPerDayRepository productsPerDayRepository;

    @Transactional
    public void replacePerDate(LocalDate date, Map<Long, Double> productQuantityMap) {
        log.info("Замена списка продуктов за дату {}, количество позиций: {}", date, productQuantityMap.size());
        productsPerDayRepository.deleteById_Date(date);
        log.debug("Удалены старые записи за {}", date);

        if (productQuantityMap.isEmpty()) {
            log.info("Список продуктов за {} пуст, сохранение не требуется", date);
            return;
        }

        List<ProductsPerDayEntity> entities = new ArrayList<>(productQuantityMap.size());
        productQuantityMap.forEach((productId, quantity) ->
                entities.add(new ProductsPerDayEntity(
                        new ProductsPerDayKeyEntity(date, productId),
                        quantity
                ))
        );
        productsPerDayRepository.saveAll(entities);
        log.info("Сохранены новые записи за {}", date);
    }
}
