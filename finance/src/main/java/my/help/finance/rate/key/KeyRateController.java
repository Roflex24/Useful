package my.help.finance.rate.key;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/key/rate")
public class KeyRateController {

    private final KeyRateService keyRateService;

    @GetMapping()
    public ResponseEntity<KeyRateModel> getKeyRate() {
        KeyRateModel rate = keyRateService.getKeyRateModel();
        return ResponseEntity.ok(rate);
    }


    @GetMapping("/all")
    public ResponseEntity<List<KeyRateModel>> getAllKeyRate() {
        return ResponseEntity.ok(keyRateService.getKeyRateModels());
    }
}