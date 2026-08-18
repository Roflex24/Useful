package my.help.food.nutrients;

import lombok.RequiredArgsConstructor;
import my.help.food.product.ProductModel;
import my.help.food.products_per_day.*;
import my.help.food.product.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NutrientsService {

    private final NutrientsRepository nutrientsRepository;
    private final NutrientsMapper nutrientsMapper;
    private final ProductsPerDayService productsPerDayService;
    private final ProductService productService;
    private final ProductsPerDayMapper productsPerDayMapper;
    private final NutritionCalculator nutritionCalculator;

    @Transactional
    public NutrientsModel addNutrientsPerDay(NutrientsModel nutrientsModel) {
        nutrientsRepository.save(nutrientsMapper.toEntity(nutrientsModel));
        Map<Long, Double> productQuantityMap = new HashMap<>();
        if (nutrientsModel.getProductsPerDay() != null) {
            for (ProductsPerDayModel productsPerDayModel : nutrientsModel.getProductsPerDay()) {
                productQuantityMap.put(productsPerDayModel.getId(), productsPerDayModel.getQuantity());
            }
        }
        productsPerDayService.addProductsPerDay(nutrientsModel.getDate(), productQuantityMap);
        return nutrientsModel;
    }

    @Transactional(readOnly = true)
    public List<NutrientsModel> getNutrientsList() {
        return nutrientsMapper.toModelList(nutrientsRepository.findAll());
    }

    @Transactional(readOnly = true)
    public NutrientsModel getNutrientsPerDate(LocalDate localDate) {
        NutrientsPerDayEntity entity = nutrientsRepository.findById(localDate)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Nutrients not found for date: " + localDate));
        NutrientsModel model = nutrientsMapper.toModel(entity);

        List<ProductsPerDayEntity> productsPerDayEntityList = productsPerDayService.getProductsPerDate(localDate);
        List<ProductsPerDayModel> productsPerDayModelList = productsPerDayEntityList.stream()
                .map(ppd -> {
                    ProductModel productModel = productService.getProductById(ppd.getId().getProductId());
                    return productsPerDayMapper.toModel(productModel, ppd.getQuantity());
                })
                .sorted(Comparator.comparing(ProductsPerDayModel::getName))
                .collect(Collectors.toList());
        model.setProductsPerDay(productsPerDayModelList);
        return model;
    }

    @Transactional(readOnly = true)
    public List<NutrientsModel> getNutrientsPerDateForWeek() {
        List<NutrientsModel> list = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (int i = 0; i < 7; i++) {
            list.add(getNutrientsPerDate(today.minusDays(i)));
        }
        return list;
    }

    public NutrientsExpenditureRs calculateDailyNutrients(NutrientsExpenditureRq request) {
        return nutritionCalculator.calculateDailyNutrients(request);
    }
}