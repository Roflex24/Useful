package my.help.useful.kanban.column;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/columns")
@RequiredArgsConstructor
public class ColumnController {

    private final ColumnService columnService;

    @GetMapping("/{id}")
    ResponseEntity<List<ColumnModel>> getColumnsByProjectId(@PathVariable Long id) {
        return ResponseEntity.ok(columnService.getColumnsByProjectId(id));
    }

    @GetMapping
    ResponseEntity<List<ColumnModel>> getAllColumns() {
        return ResponseEntity.ok(columnService.getAllColumns());
    }
}