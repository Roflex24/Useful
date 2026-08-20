package my.help.food.diet;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.help.food.common.exception.DietNotFoundException;
import my.help.food.diet.dto.*;
import my.help.food.product.ProductMapper;
import my.help.food.product.ProductRepository;
import my.help.food.product.ProductValidator;
import my.help.food.product.dto.ProductRs;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DietService {

    private final DietRepository dietRepository;
    private final DietMapper dietMapper;
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final ProductValidator productValidator;

    @Transactional(readOnly = true)
    public List<DietRs> getList() {
        log.debug("Получение всех рационов из БД");
        List<DietRs> result = dietRepository.findAllWithItems().stream()
                .map(this::toDietRsWithProducts)
                .toList();

        log.info("Возвращено рационов: {}", result.size());
        return result;
    }

    @Transactional
    public DietRs create(DietRq rq) {
        log.info("Создание рациона: name={}", rq.name());
        validateItems(rq.items());

        DietEntity entity = dietMapper.toEntity(rq);
        if (rq.items() != null) {
            List<DietItemEntity> items = rq.items().stream()
                    .map(item -> dietMapper.toItemEntity(item, entity))
                    .toList();
            entity.setItems(items);
            log.debug("Добавлено позиций в рацион: {}", items.size());
        }
        DietEntity saved = dietRepository.save(entity);
        log.info("Рацион создан с id={}", saved.getId());
        return toDietRsWithProducts(saved);
    }

    @Transactional
    public DietRs update(Long id, DietRq rq) {
        log.info("Обновление рациона id={}", id);
        DietEntity entity = dietRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Рацион с id={} не найден", id);
                    return new DietNotFoundException(id);
                });
        validateItems(rq.items());
        dietMapper.updateEntityFromRequest(rq, entity);

        entity.getItems().clear();
        if (rq.items() != null) {
            List<DietItemEntity> items = rq.items().stream()
                    .map(item -> dietMapper.toItemEntity(item, entity))
                    .toList();
            entity.getItems().addAll(items);
            log.debug("Обновлено позиций в рационе: {}", items.size());
        }
        DietEntity saved = dietRepository.save(entity);
        log.info("Рацион id={} успешно обновлён", id);
        return toDietRsWithProducts(saved);
    }

    @Transactional
    public void delete(Long id) {
        log.info("Удаление рациона id={}", id);
        if (!dietRepository.existsById(id)) {
            log.warn("Попытка удаления несуществующего рациона id={}", id);
            throw new DietNotFoundException(id);
        }
        dietRepository.deleteById(id);
        log.info("Рацион id={} удалён", id);
    }

    private void validateItems(List<DietItemRq> items) {
        if (items == null || items.isEmpty()) {
            return;
        }
        List<Long> productIds = items.stream().map(DietItemRq::productId).toList();
        productValidator.validateNoDuplicateProductIds(productIds);
        productValidator.validateExist(productIds);
    }

    private DietRs toDietRsWithProducts(DietEntity entity) {
        Set<Long> productIds = entity.getItems().stream()
                .map(DietItemEntity::getProductId)
                .collect(Collectors.toSet());

        Map<Long, ProductRs> productMap = productRepository.findAllById(productIds).stream()
                .map(productMapper::toResponse)
                .collect(Collectors.toMap(ProductRs::id, p -> p));

        List<DietItemRs> itemRsList = entity.getItems().stream()
                .map(item -> new DietItemRs(
                        item.getId(),
                        item.getProductId(),
                        item.getQuantity(),
                        productMap.get(item.getProductId())
                ))
                .toList();

        return new DietRs(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                itemRsList
        );
    }
}