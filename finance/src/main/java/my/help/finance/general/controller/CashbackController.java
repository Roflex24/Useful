package my.help.finance.general.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import my.help.finance.general.dto.CashbackRq;
import my.help.finance.general.service.CashbackService;
import my.help.finance.general.dto.BankCashbackSummaryDto;
import my.help.finance.general.dto.CashbackRs;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/finance/cashbacks")
@RequiredArgsConstructor
public class CashbackController {

    private final CashbackService cashbackService;

    @GetMapping
    public List<CashbackRs> getAllCashbacks() {
        return cashbackService.getAllCashbacks();
    }

    @GetMapping("/account/{accountId}")
    public List<CashbackRs> getCashbacksByAccount(@PathVariable Long accountId) {
        return cashbackService.getCashbacksByAccount(accountId);
    }

    @GetMapping("/summary")
    public List<BankCashbackSummaryDto> getCashbackSummary() {
        return cashbackService.getCashbackSummaryByBank();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CashbackRs createCashback(@Valid @RequestBody CashbackRq rq) {
        return cashbackService.createCashback(rq);
    }

    @PutMapping("/{id}")
    public CashbackRs updateCashback(
            @PathVariable Long id,
            @Valid @RequestBody CashbackRq rq) {
        return cashbackService.updateCashback(id, rq);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCashback(@PathVariable Long id) {
        cashbackService.deleteCashback(id);
    }
}