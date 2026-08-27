package my.help.useful.util;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/util")
@Tag(name = "Common API", description = "Общее")
public class UtilController {

    @GetMapping("/base/backup")
    ResponseEntity<Void> baseBackUp() throws IOException, InterruptedException {
        PgDumpWrapper.dumpDatabase(
                "postgres",
                "postgres",
                "123456",
                "backup.sql"
        );
        return ResponseEntity.ok().build();
    }
}
