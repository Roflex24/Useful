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

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

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

    // Получить активные кешбеки
    @GetMapping("/active")
    public ResponseEntity<List<CashbackResponseDto>> getActiveCashbacks() {
        return ResponseEntity.ok(cashbackService.getActiveCashbacks());
    }

    // Получить сводку кешбека по банкам
    @GetMapping("/summary")
    public ResponseEntity<List<BankCashbackSummaryDto>> getCashbackSummary() {
        return ResponseEntity.ok(cashbackService.getCashbackSummaryByBank());
    }

    // Получить лучший кешбек по категориям
    @GetMapping("/best-by-category")
    public ResponseEntity<Map<String, BigDecimal>> getBestCashbackByCategory() {
        return ResponseEntity.ok(cashbackService.getBestCashbackForCategories());
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

    // Деактивировать кешбек
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivateCashback(@PathVariable Long id) {
        cashbackService.deactivateCashback(id);
        return ResponseEntity.noContent().build();
    }

    // Удалить кешбек
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCashback(@PathVariable Long id) {
        cashbackService.deleteCashback(id);
        return ResponseEntity.noContent().build();
    }
}