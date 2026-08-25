package my.help.finance.general.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import my.help.finance.general.service.AccountService;
import my.help.finance.general.service.FinanceSummaryService;
import my.help.finance.general.dto.AccountRequestDto;
import my.help.finance.general.dto.AccountResponseDto;
import my.help.finance.general.dto.FinanceSummaryDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<FinanceSummaryDto> getFinanceSummary() {
        FinanceSummaryDto summary = financeSummaryService.getFinanceSummary();
        return ResponseEntity.ok(summary);
    }

    // Получить все счета
    @GetMapping("/accounts")
    public ResponseEntity<List<AccountResponseDto>> getAllAccounts() {
        List<AccountResponseDto> accounts = accountService.getAllAccounts();
        return ResponseEntity.ok(accounts);
    }

    // Получить счёт по ID
    @GetMapping("/accounts/{id}")
    public ResponseEntity<AccountResponseDto> getAccountById(@PathVariable Long id) {
        AccountResponseDto account = accountService.getAccountById(id);
        return ResponseEntity.ok(account);
    }

    // Создать счёт
    @PostMapping("/accounts")
    public ResponseEntity<AccountResponseDto> createAccount(@Valid @RequestBody AccountRequestDto requestDto) {
        AccountResponseDto createdAccount = accountService.createAccount(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdAccount);
    }

    // Обновить счёт
    @PutMapping("/accounts/{id}")
    public ResponseEntity<AccountResponseDto> updateAccount(
            @PathVariable Long id,
            @Valid @RequestBody AccountRequestDto requestDto) {
        AccountResponseDto updatedAccount = accountService.updateAccount(id, requestDto);
        return ResponseEntity.ok(updatedAccount);
    }

    // Удалить счёт
    @DeleteMapping("/accounts/{id}")
    public ResponseEntity<Void> deleteAccount(@PathVariable Long id) {
        accountService.deleteAccount(id);
        return ResponseEntity.noContent().build();
    }
}