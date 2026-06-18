package my.help.kanban.column;

import lombok.RequiredArgsConstructor;
import my.help.kanban.project.ProjectEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ColumnService {

    private final ColumnRepository columnRepository;
    private final ColumnMapper columnMapper;

    public List<ColumnModel> getColumnsByProjectId(Long projectId) {
        return columnMapper.toModelList(columnRepository.findAllByProjectId(projectId));
    }

    public List<ColumnModel> getAllColumns() {
        return columnMapper.toModelList(columnRepository.findAll());
    }

    public void createColumns(ProjectEntity projectEntity) {
        List<String> columnNameList = List.of("Бэклог", "На ближайшие дни", "В процессе", "Готово");
        List<ColumnEntity> columnEntityList = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            columnEntityList.add(new ColumnEntity(null, columnNameList.get(i), i, projectEntity));
        }
        columnRepository.saveAll(columnEntityList);
    }
}
