package my.help.kanban.column;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/column")
@RequiredArgsConstructor
@Tag(name = "Kanban API", description = "Раздел планировая")
public class ColumnController {

    private final ColumnService columnService;

    @GetMapping("/{id}")
    List<ColumnModel> getByProjectId(@PathVariable Long id) {
        return columnService.getByProjectId(id);
    }

    @GetMapping
    List<ColumnModel> getList() {
        return columnService.getList();
    }
}