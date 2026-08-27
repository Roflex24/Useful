package my.help.finance.exchange_rate;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/currency/rate")
public class CurrencyRateController {

    private final CbrCurrencyRateProvider cbrCurrencyRateProvider;

    @GetMapping()
    public ResponseEntity<CurrencyRateModel> getCurrencyRate() throws Exception {
        return ResponseEntity.ok(cbrCurrencyRateProvider.getCurrencyRateModel());
    }

    @GetMapping("/all")
    public ResponseEntity<List<CurrencyRateModel>> getAllCurrencyRate() throws Exception {
        return ResponseEntity.ok(cbrCurrencyRateProvider.getCurrencyRateModels());
    }
}
