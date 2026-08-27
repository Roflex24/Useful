package my.help.finance.general.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import my.help.finance.general.dto.SecurityRq;
import my.help.finance.general.dto.SecurityRs;
import my.help.finance.general.service.SecurityService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/finance/securities")
@RequiredArgsConstructor
@Tag(name = "Finance API", description = "Раздел финансов")
public class SecurityController {

    private final SecurityService securityService;

    @GetMapping
    public List<SecurityRs> getAllSecurities() {
        return securityService.getAllSecurities();
    }

    @GetMapping("/account/{accountId}")
    public List<SecurityRs> getSecuritiesByAccount(@PathVariable Long accountId) {
        return securityService.getSecuritiesByAccount(accountId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SecurityRs createSecurity(@Valid @RequestBody SecurityRq rq) {
        return securityService.createSecurity(rq);
    }

    @PutMapping("/{id}")
    public SecurityRs updateSecurity(
            @PathVariable Long id,
            @Valid @RequestBody SecurityRq rq) {
        return securityService.updateSecurity(id, rq);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSecurity(@PathVariable Long id) {
        securityService.deleteSecurity(id);
    }
}