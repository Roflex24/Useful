package my.help.finance.general.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import my.help.finance.general.dto.SecurityRequestDto;
import my.help.finance.general.dto.SecurityResponseDto;
import my.help.finance.general.service.SecurityService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/finance/securities")
@RequiredArgsConstructor
public class SecurityController {

    private final SecurityService securityService;

    // Получить все бумаги
    @GetMapping
    public List<SecurityResponseDto> getAllSecurities() {
        return securityService.getAllSecurities();
    }

    // Получить бумаги по счёту
    @GetMapping("/account/{accountId}")
    public List<SecurityResponseDto> getSecuritiesByAccount(@PathVariable Long accountId) {
        return securityService.getSecuritiesByAccount(accountId);
    }

    // Создать бумагу
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SecurityResponseDto createSecurity(@Valid @RequestBody SecurityRequestDto requestDto) {
        return securityService.createSecurity(requestDto);
    }

    // Обновить бумагу
    @PutMapping("/{id}")
    public SecurityResponseDto updateSecurity(
            @PathVariable Long id,
            @Valid @RequestBody SecurityRequestDto requestDto) {
        return securityService.updateSecurity(id, requestDto);
    }

    // Удалить бумагу
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSecurity(@PathVariable Long id) {
        securityService.deleteSecurity(id);
    }
}