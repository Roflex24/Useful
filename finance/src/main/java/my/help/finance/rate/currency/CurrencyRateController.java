package my.help.finance.rate.currency;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/currency/rate")
@Tag(name = "Finance API", description = "Раздел финансов")
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
