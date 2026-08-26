package my.help.kanban.metric;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectMetricRepository extends JpaRepository<ProjectMetricEntity, Long> {

    List<ProjectMetricEntity> findAllByProjectId(Long projectId);
    void deleteByProjectId(Long projectId);
}
