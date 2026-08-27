package my.help.kanban.column;

import lombok.RequiredArgsConstructor;
import my.help.kanban.project.Project;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ColumnService {

    private final ColumnRepository columnRepository;
    private final ColumnMapper columnMapper;

    public List<ColumnModel> getByProjectId(Long projectId) {
        return columnMapper.toModelList(columnRepository.findAllByProjectId(projectId));
    }

    public List<ColumnModel> getList() {
        return columnMapper.toModelList(columnRepository.findAll());
    }

    public void createColumns(Project project) {
        List<String> columnNameList = List.of("Бэклог", "На ближайшие дни", "В процессе", "Готово");
        List<Column> columnList = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            columnList.add(new Column(null, columnNameList.get(i), i, project));
        }
        columnRepository.saveAll(columnList);
    }
}
