package my.help.kanban.column;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/column")
@RequiredArgsConstructor
public class ColumnController {

    private final ColumnService columnService;

    @GetMapping("/{id}")
    List<ColumnModel> getByProjectId(@PathVariable Long id) {
        return columnService.getByProjectId(id);
    }

    @GetMapping
    List<ColumnModel> getAll() {
        return columnService.getAll();
    }
}