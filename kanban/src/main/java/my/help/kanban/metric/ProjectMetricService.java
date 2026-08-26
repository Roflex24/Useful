package my.help.kanban.metric;

import lombok.RequiredArgsConstructor;
import my.help.kanban.common.ResourceNotFoundException;
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

    public void createProjectMetric(ProjectMetricRq rq) {
        ProjectMetricEntity projectMetricEntity = projectMetricMapper.projectMetricRqToEntity(rq);
        projectMetricEntity.setProject(projectRepository.findById(rq.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Проект с id=" + rq.getProjectId() + " не найдено")));
        projectMetricRepository.save(projectMetricEntity);
    }

    public List<ProjectMetricModel> getMetricByProjectId(Long projectId) {
        List<ProjectMetricModel> projectMetricModelList = projectMetricMapper.toModelList(projectMetricRepository.findAllByProjectId(projectId));
        for (ProjectMetricModel model: projectMetricModelList) {
            model.setProjectId(projectId);
        }
        return projectMetricModelList;
    }

    public void updateProjectMetricList(List<ProjectMetricModel> list) {
        List<ProjectMetricEntity> projectMetricEntityList = projectMetricMapper.toEntityList(list);
        Long id = list.getFirst().getProjectId();
        for (ProjectMetricEntity projectMetricEntity: projectMetricEntityList) {
            projectMetricEntity.setProject(projectRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Проект с id=" + id + " не найдено")));
        }
        projectMetricRepository.saveAll(projectMetricEntityList);
    }

    @Transactional
    public ProjectMetricModel updateProjectMetric(Long id, ProjectMetricRq rq) {
        ProjectMetricEntity projectMetricEntity = projectMetricRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Метрика проекта с id=" + id + " не найдено"));
        projectMetricEntity.setName(rq.getName());
        projectMetricEntity.setComplete(rq.isComplete());
        projectMetricEntity.setMain(rq.isMain());
        projectMetricEntity.setOrderIndex(rq.getOrderIndex());

        return projectMetricMapper.toModel(projectMetricEntity);
    }


    public void deleteProjectById(Long id) {
        projectMetricRepository.deleteById(id);
    }
}
