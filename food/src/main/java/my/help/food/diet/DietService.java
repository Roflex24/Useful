package my.help.food.diet;

import lombok.RequiredArgsConstructor;
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
    public List<DietResponse> getAllDiets() {
        return dietMapper.toResponseList(dietRepository.findAll());
    }

    @Transactional
    public DietResponse createDiet(DietRequest request) {
        DietEntity entity = dietMapper.toEntity(request);
        if (request.items() != null) {
            List<DietItemEntity> items = request.items().stream()
                    .map(item -> dietMapper.toItemEntity(item, entity))
                    .toList();
            entity.setItems(items);
        }
        return dietMapper.toResponse(dietRepository.save(entity));
    }

    @Transactional
    public DietResponse updateDiet(Long id, DietRequest request) {
        DietEntity entity = dietRepository.findById(id)
                .orElseThrow(() -> new DietNotFoundException(id));
        dietMapper.updateEntityFromRequest(request, entity);

        // Полная замена списка items
        entity.getItems().clear();
        if (request.items() != null) {
            List<DietItemEntity> items = request.items().stream()
                    .map(item -> dietMapper.toItemEntity(item, entity))
                    .toList();
            entity.getItems().addAll(items);
        }
        return dietMapper.toResponse(dietRepository.save(entity));
    }

    @Transactional
    public void deleteDiet(Long id) {
        if (!dietRepository.existsById(id)) {
            throw new DietNotFoundException(id);
        }
        dietRepository.deleteById(id);
    }
}