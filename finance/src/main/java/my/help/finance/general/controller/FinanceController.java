package my.help.finance.general.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import my.help.finance.general.service.AccountService;
import my.help.finance.general.service.FinanceSummaryService;
import my.help.finance.general.dto.AccountRq;
import my.help.finance.general.dto.AccountRs;
import my.help.finance.general.dto.FinanceSummaryDto;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/finance")
@RequiredArgsConstructor
public class FinanceController {

    private final AccountService accountService;
    private final FinanceSummaryService financeSummaryService;

    @GetMapping("/summary")
    public FinanceSummaryDto getFinanceSummary() {
        return financeSummaryService.getFinanceSummary();
    }

    @GetMapping("/accounts")
    public List<AccountRs> getAllAccounts() {
        return accountService.getAllAccounts();
    }

    @GetMapping("/accounts/{id}")
    public AccountRs getAccountById(@PathVariable Long id) {
        return accountService.getAccountById(id);
    }

    @PostMapping("/accounts")
    @ResponseStatus(HttpStatus.CREATED)
    public AccountRs createAccount(@Valid @RequestBody AccountRq rq) {
        return accountService.createAccount(rq);
    }

    @PutMapping("/accounts/{id}")
    public AccountRs updateAccount(
            @PathVariable Long id,
            @Valid @RequestBody AccountRq rq) {
        return accountService.updateAccount(id, rq);
    }

    @DeleteMapping("/accounts/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAccount(@PathVariable Long id) {
        accountService.deleteAccount(id);
    }
}