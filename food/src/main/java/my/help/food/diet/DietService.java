package my.help.food.diet;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DietService {

    private final DietRepository dietRepository;
    private final DietMapper dietMapper;

    @Transactional(readOnly = true)
    public List<DietModel> getAllDiets() {
        return dietMapper.toModelList(dietRepository.findAll());
    }

    @Transactional
    public DietModel createDiet(DietModel dietModel) {
        DietEntity dietEntity = dietMapper.toEntity(dietModel);
        if (dietModel.getItems() != null) {
            List<DietItemEntity> items = dietModel.getItems().stream()
                    .map(item -> dietMapper.toItemEntity(item, dietEntity))
                    .toList();
            dietEntity.setItems(items);
        }
        return dietMapper.toModel(dietRepository.save(dietEntity));
    }

    @Transactional
    public DietModel updateDiet(DietModel dietModel) {
        DietEntity dietEntity = dietRepository.findById(dietModel.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Diet not found"));
        dietMapper.updateEntityFromModel(dietModel, dietEntity);
        dietEntity.getItems().clear();
        if (dietModel.getItems() != null) {
            List<DietItemEntity> items = dietModel.getItems().stream()
                    .map(item -> dietMapper.toItemEntity(item, dietEntity))
                    .toList();
            dietEntity.getItems().addAll(items);
        }
        return dietMapper.toModel(dietRepository.save(dietEntity));
    }

    @Transactional
    public void deleteDiet(Long id) {
        if (!dietRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Diet not found");
        }
        dietRepository.deleteById(id);
    }
}