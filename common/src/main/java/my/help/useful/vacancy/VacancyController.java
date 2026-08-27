package my.help.useful.vacancy;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/vacancy")
@Tag(name = "Common API", description = "Общее")
public class VacancyController {

    private final VacancyService vacancyService;

    @GetMapping("/count")
    public VacancyRs get() {
        return vacancyService.get();
    }

    @GetMapping("/list")
    public List<VacancyRs> getList() {
        return vacancyService.getList();
    }
}
