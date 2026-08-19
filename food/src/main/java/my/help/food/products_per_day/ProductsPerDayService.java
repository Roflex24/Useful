package my.help.food.products_per_day;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
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
        productQuantityMap.forEach((productId, quantity) ->
                productsPerDayRepository.save(new ProductsPerDayEntity(
                        new ProductsPerDayKeyEntity(date, productId),
                        quantity
                ))
        );
        log.info("Сохранены новые записи за {}", date);
    }
}