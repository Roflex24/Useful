package my.help.finance.invest;

import org.apache.hc.core5.http.ParseException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/ofz")
public class OFZController {

    private final MoexService moexService;

    public OFZController(MoexService moexService) {
        this.moexService = moexService;
    }

    @GetMapping
    public List<OFZBondSummary> getAllOFZ() throws IOException, ParseException {
        return moexService.fetchOFZDataWithStats();
    }
}