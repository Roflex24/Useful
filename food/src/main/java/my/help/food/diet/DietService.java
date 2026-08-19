package my.help.food.diet;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.help.food.common.exception.DietNotFoundException;
import my.help.food.diet.dto.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DietService {

    private final DietRepository dietRepository;
    private final DietMapper dietMapper;

    @Transactional(readOnly = true)
    public List<DietRs> getList() {
        log.debug("Получение всех рационов из БД");
        List<DietRs> result = dietMapper.toResponseList(dietRepository.findAllWithItems());
        log.info("Возвращено рационов: {}", result.size());
        return result;
    }

    @Transactional
    public DietRs create(DietRq rq) {
        log.info("Создание рациона: name={}", rq.name());
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
        return dietMapper.toResponse(saved);
    }

    @Transactional
    public DietRs update(Long id, DietRq rq) {
        log.info("Обновление рациона id={}", id);
        DietEntity entity = dietRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Рацион с id={} не найден", id);
                    return new DietNotFoundException(id);
                });
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
        return dietMapper.toResponse(saved);
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
}