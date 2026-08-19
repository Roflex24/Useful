package my.help.food.products_per_day;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ProductsPerDayService {

    private final ProductsPerDayRepository productsPerDayRepository;

    @Transactional
    public void replacePerDay(LocalDate date, Map<Long, Double> productQuantityMap) {
        productsPerDayRepository.deleteById_Date(date);
        productQuantityMap.forEach((productId, quantity) ->
                productsPerDayRepository.save(new ProductsPerDayEntity(
                        new ProductsPerDayKeyEntity(date, productId),
                        quantity
                ))
        );
    }

    @Transactional(readOnly = true)
    public List<ProductsPerDayEntity> getPerDate(LocalDate localDate) {
        return productsPerDayRepository.findById_Date(localDate);
    }

    @Transactional(readOnly = true)
    public List<ProductsPerDayEntity> getBetween(LocalDate start, LocalDate end) {
        return productsPerDayRepository.findById_DateBetween(start, end);
    }
}