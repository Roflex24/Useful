package my.help.finance.invest.bond;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/ofz")
@RequiredArgsConstructor
public class OFZController {

    private final MoexService moexService;

    @GetMapping
    public List<OFZBondSummary> getAllOFZ() throws IOException {
        return moexService.fetchOFZDataWithStats();
    }
}