package my.help.finance.general.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import my.help.finance.general.service.AccountService;
import my.help.finance.general.service.FinanceSummaryService;
import my.help.finance.general.dto.AccountRequestDto;
import my.help.finance.general.dto.AccountResponseDto;
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

    // Сводка
    @GetMapping("/summary")
    public FinanceSummaryDto getFinanceSummary() {
        return financeSummaryService.getFinanceSummary();
    }

    // Получить все счета
    @GetMapping("/accounts")
    public List<AccountResponseDto> getAllAccounts() {
        return accountService.getAllAccounts();
    }

    // Получить счёт по ID
    @GetMapping("/accounts/{id}")
    public AccountResponseDto getAccountById(@PathVariable Long id) {
        return accountService.getAccountById(id);
    }

    // Создать счёт
    @PostMapping("/accounts")
    @ResponseStatus(HttpStatus.CREATED)
    public AccountResponseDto createAccount(@Valid @RequestBody AccountRequestDto requestDto) {
        return accountService.createAccount(requestDto);
    }

    // Обновить счёт
    @PutMapping("/accounts/{id}")
    public AccountResponseDto updateAccount(
            @PathVariable Long id,
            @Valid @RequestBody AccountRequestDto requestDto) {
        return accountService.updateAccount(id, requestDto);
    }

    // Удалить счёт
    @DeleteMapping("/accounts/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAccount(@PathVariable Long id) {
        accountService.deleteAccount(id);
    }
}