package my.help.kanban.column;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ColumnRepository extends JpaRepository<Column, Long> {

    List<Column> findAllByProjectId(Long id);
    Column findByProjectIdAndOrderIndex(Long project_id, int orderIndex);
    void deleteByProjectId(Long id);

}
