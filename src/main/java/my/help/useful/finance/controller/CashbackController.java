package my.help.useful.finance.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import my.help.useful.finance.dto.BankCashbackSummaryDto;
import my.help.useful.finance.dto.CashbackRequestDto;
import my.help.useful.finance.dto.CashbackResponseDto;
import my.help.useful.finance.service.CashbackService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/finance/cashbacks")
@RequiredArgsConstructor
public class CashbackController {

    private final CashbackService cashbackService;

    // Получить все кешбеки
    @GetMapping
    public ResponseEntity<List<CashbackResponseDto>> getAllCashbacks() {
        return ResponseEntity.ok(cashbackService.getAllCashbacks());
    }

    // Получить кешбеки по счёту
    @GetMapping("/account/{accountId}")
    public ResponseEntity<List<CashbackResponseDto>> getCashbacksByAccount(@PathVariable Long accountId) {
        return ResponseEntity.ok(cashbackService.getCashbacksByAccount(accountId));
    }

    // Получить сводку кешбека по банкам
    @GetMapping("/summary")
    public ResponseEntity<List<BankCashbackSummaryDto>> getCashbackSummary() {
        return ResponseEntity.ok(cashbackService.getCashbackSummaryByBank());
    }

    // Создать кешбек
    @PostMapping
    public ResponseEntity<CashbackResponseDto> createCashback(@Valid @RequestBody CashbackRequestDto requestDto) {
        CashbackResponseDto createdCashback = cashbackService.createCashback(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCashback);
    }

    // Обновить кешбек
    @PutMapping("/{id}")
    public ResponseEntity<CashbackResponseDto> updateCashback(
            @PathVariable Long id,
            @Valid @RequestBody CashbackRequestDto requestDto) {
        CashbackResponseDto updatedCashback = cashbackService.updateCashback(id, requestDto);
        return ResponseEntity.ok(updatedCashback);
    }

    // Удалить кешбек
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCashback(@PathVariable Long id) {
        cashbackService.deleteCashback(id);
        return ResponseEntity.noContent().build();
    }
}