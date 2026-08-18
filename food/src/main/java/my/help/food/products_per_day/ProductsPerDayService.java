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
    public void addProductsPerDay(LocalDate date, Map<Long, Double> productQuantityMap) {
        productsPerDayRepository.deleteById_Date(date);
        for (Map.Entry<Long, Double> entry : productQuantityMap.entrySet()) {
            productsPerDayRepository.save(new ProductsPerDayEntity(
                    new ProductsPerDayKeyEntity(date, entry.getKey()),
                    entry.getValue()
            ));
        }
    }

    @Transactional(readOnly = true)
    public List<ProductsPerDayEntity> getProductsPerDate(LocalDate localDate) {
        return productsPerDayRepository.findById_Date(localDate);
    }
}