package my.help.useful.kanban.column;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ColumnRepository extends JpaRepository<ColumnEntity, Long> {

    List<ColumnEntity> findAllByProjectId(Long id);
    ColumnEntity findByProjectIdAndOrderIndex(Long project_id, int orderIndex);
    void deleteByProjectId(Long id);

}
