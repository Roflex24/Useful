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

    // Получить все кешбеки
    @GetMapping
    public List<CashbackRs> getAllCashbacks() {
        return cashbackService.getAllCashbacks();
    }

    // Получить кешбеки по счёту
    @GetMapping("/account/{accountId}")
    public List<CashbackRs> getCashbacksByAccount(@PathVariable Long accountId) {
        return cashbackService.getCashbacksByAccount(accountId);
    }

    // Получить сводку кешбека по банкам
    @GetMapping("/summary")
    public List<BankCashbackSummaryDto> getCashbackSummary() {
        return cashbackService.getCashbackSummaryByBank();
    }

    // Создать кешбек
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CashbackRs createCashback(@Valid @RequestBody CashbackRq requestDto) {
        return cashbackService.createCashback(requestDto);
    }

    // Обновить кешбек
    @PutMapping("/{id}")
    public CashbackRs updateCashback(
            @PathVariable Long id,
            @Valid @RequestBody CashbackRq requestDto) {
        return cashbackService.updateCashback(id, requestDto);
    }

    // Удалить кешбек
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCashback(@PathVariable Long id) {
        cashbackService.deleteCashback(id);
    }
}