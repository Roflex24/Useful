package my.help.useful.vacancy;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VacancyService {

    private final VacancyRepository vacancyRepository;

    public VacancyModel getVacancyModel() {
        LocalDate date = LocalDate.now();

        Optional<VacancyEntity> vacancyEntityOptional = vacancyRepository.findByDate(date);
        if (vacancyEntityOptional.isPresent()) {
            VacancyEntity vacancyEntity = vacancyEntityOptional.get();
            return new VacancyModel(vacancyEntity.getDate(), vacancyEntity.getVacancyCount());
        } else {
            int vacancyCount = HhVacancyParserJsoup.getVacancyCount();
            vacancyRepository.save(new VacancyEntity(date , vacancyCount));
            return new VacancyModel(date, vacancyCount);
        }
    }

    public List<VacancyModel> getVacancyModelList() {
        List<VacancyEntity> vacancyEntityList = vacancyRepository.findAll();
        List<VacancyModel> vacancyModelList = new ArrayList<>();

        for (VacancyEntity vacancyEntity : vacancyEntityList) {
            vacancyModelList.add(new VacancyModel(vacancyEntity.getDate(), vacancyEntity.getVacancyCount()));
        }

        return vacancyModelList;
    }
}
