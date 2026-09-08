package my.help.finance.invest.deposit;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/deposits")
@RequiredArgsConstructor
public class DepositController {

    private final DepositParserService parserService;

    @GetMapping("/rates")
    public DepositRatesResponse getDepositRates(Pageable pageable) {
        return parserService.getDepositRatesForToday(pageable);
    }
}