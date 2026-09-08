package my.help.finance.invest.bond;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
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
    public List<OFZBondSummary> getAllOFZ(Pageable pageable) throws IOException {
        return moexService.fetchOFZDataWithStats(pageable);
    }
}