package my.help.finance.rate.key;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/key/rate")
@Tag(name = "Finance API", description = "Раздел финансов")
public class KeyRateController {

    private final KeyRateService keyRateService;

    @GetMapping()
    public KeyRateRs get() {
        return keyRateService.get();
    }


    @GetMapping("/all")
    public List<KeyRateRs> getList() {
        return keyRateService.getList();
    }
}