package my.help.finance.general.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import my.help.finance.general.dto.SecurityRequestDto;
import my.help.finance.general.dto.SecurityResponseDto;
import my.help.finance.general.service.SecurityService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/finance/securities")
@RequiredArgsConstructor
public class SecurityController {

    private final SecurityService securityService;

    // Получить все бумаги
    @GetMapping
    public ResponseEntity<List<SecurityResponseDto>> getAllSecurities() {
        return ResponseEntity.ok(securityService.getAllSecurities());
    }

    // Получить бумаги по счёту
    @GetMapping("/account/{accountId}")
    public ResponseEntity<List<SecurityResponseDto>> getSecuritiesByAccount(@PathVariable Long accountId) {
        return ResponseEntity.ok(securityService.getSecuritiesByAccount(accountId));
    }

    // Создать бумагу
    @PostMapping
    public ResponseEntity<SecurityResponseDto> createSecurity(@Valid @RequestBody SecurityRequestDto requestDto) {
        SecurityResponseDto created = securityService.createSecurity(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // Обновить бумагу
    @PutMapping("/{id}")
    public ResponseEntity<SecurityResponseDto> updateSecurity(
            @PathVariable Long id,
            @Valid @RequestBody SecurityRequestDto requestDto) {
        SecurityResponseDto updated = securityService.updateSecurity(id, requestDto);
        return ResponseEntity.ok(updated);
    }

    // Удалить бумагу
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSecurity(@PathVariable Long id) {
        securityService.deleteSecurity(id);
        return ResponseEntity.noContent().build();
    }
}