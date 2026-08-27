package my.help.useful.vacancy;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VacancyService {

    private final VacancyRepository vacancyRepository;

    public VacancyRs get() {
        LocalDate date = LocalDate.now();

        Optional<Vacancy> vacancyEntityOptional = vacancyRepository.findByDate(date);
        if (vacancyEntityOptional.isPresent()) {
            Vacancy vacancy = vacancyEntityOptional.get();
            return new VacancyRs(vacancy.getDate(), vacancy.getVacancyCount());
        } else {
            int vacancyCount = HhVacancyParserJsoup.getVacancyCount();
            vacancyRepository.save(new Vacancy(date , vacancyCount));
            return new VacancyRs(date, vacancyCount);
        }
    }

    public List<VacancyRs> getList() {
        return vacancyRepository.findAll().stream()
                .map(e -> new VacancyRs(e.getDate(), e.getVacancyCount()))
                .toList();
    }
}
