package my.help.finance;


import org.apache.hc.core5.http.ParseException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api/ofz")
public class OFZController {

    private final MoexService moexService;

    public OFZController(MoexService moexService) {
        this.moexService = moexService;
    }

    @GetMapping
    public OFZResponse getAllOFZ() throws IOException, ParseException {
        return moexService.fetchOFZDataWithStats();
    }

    // Дополнительно можно добавить эндпоинт только для статистики
    @GetMapping("/stats")
    public OFZResponse getOFZStats() throws IOException, ParseException {
        return moexService.fetchOFZDataWithStats();
    }
}