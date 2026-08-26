package my.help.kanban.column;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/columns")
@RequiredArgsConstructor
public class ColumnController {

    private final ColumnService columnService;

    @GetMapping("/{id}")
    List<ColumnModel> getColumnsByProjectId(@PathVariable Long id) {
        return columnService.getColumnsByProjectId(id);
    }

    @GetMapping
    List<ColumnModel> getAllColumns() {
        return columnService.getAllColumns();
    }
}