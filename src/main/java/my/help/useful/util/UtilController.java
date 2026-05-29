package my.help.useful.util;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/util")
public class UtilController {

    @GetMapping("/base/backup")
    ResponseEntity<Void> baseBackUp() throws IOException, InterruptedException {
        PgDumpWrapper.dumpDatabase(
                "postgres",     // dbName (из spring.datasource.url)
                "postgres",     // user (из spring.datasource.username)
                "123456",       // password (из spring.datasource.password)
                "backup.sql"    // выходной файл
        );
        return ResponseEntity.ok().build();
    }
}
