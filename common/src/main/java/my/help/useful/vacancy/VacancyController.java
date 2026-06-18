package my.help.useful.vacancy;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/vacancy")
public class VacancyController {

    private final VacancyService vacancyService;

    @GetMapping("/count")
    public ResponseEntity<VacancyModel> getVacancy() {
        return ResponseEntity.ok(vacancyService.getVacancyModel());
    }

    @GetMapping("/list")
    public ResponseEntity<List<VacancyModel>> getVacancyList() {
        return ResponseEntity.ok(vacancyService.getVacancyModelList());
    }
}
