package my.help.useful.util;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import org.springframework.beans.factory.annotation.Value;

@RestController
@RequestMapping("/util")
@Tag(name = "Common API", description = "Общее")
public class UtilController {
    @Value("${spring.datasource.url}")
    private String databaseUrl;
    @Value("${spring.datasource.username}")
    private String databaseUser;
    @Value("${spring.datasource.password}")
    private String databasePassword;
    @Value("${backup.pg-dump:pg_dump}")
    private String pgDump;


    @GetMapping("/base/backup")
    ResponseEntity<Void> baseBackUp() throws IOException, InterruptedException {
        PgDumpWrapper.dumpDatabase(
                pgDump,
                databaseUrl,
                databaseUser,
                databasePassword,
                "backup.sql"
        );
        return ResponseEntity.ok().build();
    }
}
