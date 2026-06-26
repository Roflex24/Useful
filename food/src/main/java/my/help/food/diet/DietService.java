package my.help.food.diet;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DietService {

    private final DietRepository dietRepository;
    private final DietMapper dietMapper;

    public List<DietModel> getAllDiets() {
        return dietMapper.toModelList(dietRepository.findAll());
    }

    @Transactional
    public DietModel createDiet(DietModel dietModel) {
        DietEntity dietEntity = new DietEntity();
        dietEntity.setName(dietModel.getName());
        dietEntity.setDescription(dietModel.getDescription());
        dietEntity.setItems(toItemEntities(dietModel, dietEntity));

        return dietMapper.toModel(dietRepository.save(dietEntity));
    }

    @Transactional
    public DietModel updateDiet(DietModel dietModel) {
        DietEntity dietEntity = dietRepository.findById(dietModel.getId())
                .orElseThrow(() -> new RuntimeException("Diet not found"));

        dietEntity.setName(dietModel.getName());
        dietEntity.setDescription(dietModel.getDescription());

        dietEntity.getItems().clear();
        dietEntity.getItems().addAll(toItemEntities(dietModel, dietEntity));

        return dietMapper.toModel(dietRepository.save(dietEntity));
    }

    @Transactional
    public void deleteDiet(Long id) {
        dietRepository.deleteById(id);
    }

    private List<DietItemEntity> toItemEntities(DietModel dietModel, DietEntity dietEntity) {
        if (dietModel.getItems() == null) {
            return List.of();
        }
        return dietModel.getItems().stream()
                .map(item -> new DietItemEntity(null, dietEntity, item.getProductId(), item.getQuantity()))
                .collect(Collectors.toList());
    }
}
