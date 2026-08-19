package my.help.food.diet;

import lombok.RequiredArgsConstructor;
import my.help.food.common.exception.DietNotFoundException;
import my.help.food.diet.dto.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DietService {

    private final DietRepository dietRepository;
    private final DietMapper dietMapper;

    @Transactional(readOnly = true)
    public List<DietRs> getList() {
        return dietMapper.toResponseList(dietRepository.findAllWithItems());
    }

    @Transactional
    public DietRs create(DietRq rq) {
        DietEntity entity = dietMapper.toEntity(rq);
        if (rq.items() != null) {
            List<DietItemEntity> items = rq.items().stream()
                    .map(item -> dietMapper.toItemEntity(item, entity))
                    .toList();
            entity.setItems(items);
        }
        return dietMapper.toResponse(dietRepository.save(entity));
    }

    @Transactional
    public DietRs update(Long id, DietRq rq) {
        DietEntity entity = dietRepository.findById(id)
                .orElseThrow(() -> new DietNotFoundException(id));
        dietMapper.updateEntityFromRequest(rq, entity);

        entity.getItems().clear();
        if (rq.items() != null) {
            List<DietItemEntity> items = rq.items().stream()
                    .map(item -> dietMapper.toItemEntity(item, entity))
                    .toList();
            entity.getItems().addAll(items);
        }
        return dietMapper.toResponse(dietRepository.save(entity));
    }

    @Transactional
    public void delete(Long id) {
        if (!dietRepository.existsById(id)) {
            throw new DietNotFoundException(id);
        }
        dietRepository.deleteById(id);
    }
}