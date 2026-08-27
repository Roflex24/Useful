package my.help.kanban.metric;

import lombok.RequiredArgsConstructor;
import my.help.kanban.common.ResourceNotFoundException;
import my.help.kanban.metric.dto.ProjectMetricRs;
import my.help.kanban.metric.dto.ProjectMetricRq;
import my.help.kanban.project.ProjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectMetricService {

    private final ProjectMetricRepository projectMetricRepository;
    private final ProjectMetricMapper projectMetricMapper;
    private final ProjectRepository projectRepository;

    @Transactional
    public void create(ProjectMetricRq rq) {
        ProjectMetric projectMetric = projectMetricMapper.projectMetricRqToEntity(rq);
        projectMetric.setProject(projectRepository.findById(rq.projectId())
                .orElseThrow(() -> new ResourceNotFoundException("Проект с id=" + rq.projectId() + " не найдено")));
        projectMetricRepository.save(projectMetric);
    }

    public List<ProjectMetricRs> getListByProjectId(Long projectId) {
        List<ProjectMetricRs> projectMetricRsList = projectMetricMapper.toModelList(projectMetricRepository.findAllByProjectId(projectId));
        for (ProjectMetricRs model: projectMetricRsList) {
            model.setProjectId(projectId);
        }
        return projectMetricRsList;
    }

    @Transactional
    public void updateList(List<ProjectMetricRs> rq) {
        List<ProjectMetric> projectMetricList = projectMetricMapper.toEntityList(rq);
        Long id = rq.getFirst().getProjectId();
        for (ProjectMetric projectMetric : projectMetricList) {
            projectMetric.setProject(projectRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Проект с id=" + id + " не найдено")));
        }
        projectMetricRepository.saveAll(projectMetricList);
    }

    @Transactional
    public ProjectMetricRs update(Long id, ProjectMetricRq rq) {
        ProjectMetric projectMetric = projectMetricRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Метрика проекта с id=" + id + " не найдено"));
        projectMetric.setName(rq.name());
        projectMetric.setComplete(rq.isComplete());
        projectMetric.setMain(rq.isMain());
        projectMetric.setOrderIndex(rq.orderIndex());

        return projectMetricMapper.toModel(projectMetric);
    }


    public void delete(Long id) {
        projectMetricRepository.deleteById(id);
    }
}
