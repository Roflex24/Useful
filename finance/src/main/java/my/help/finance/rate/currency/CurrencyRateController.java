package my.help.finance.rate.currency;

import lombok.RequiredArgsConstructor;
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
    public CurrencyRateRs get() throws Exception {
        return cbrCurrencyRateProvider.get();
    }

    @GetMapping("/all")
    public List<CurrencyRateRs> getList() {
        return cbrCurrencyRateProvider.getList();
    }
}
