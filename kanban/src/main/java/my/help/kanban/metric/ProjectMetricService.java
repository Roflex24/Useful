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

    @Transactional
    public void create(ProjectMetricRq rq) {
        ProjectMetricEntity projectMetricEntity = projectMetricMapper.projectMetricRqToEntity(rq);
        projectMetricEntity.setProject(projectRepository.findById(rq.projectId())
                .orElseThrow(() -> new ResourceNotFoundException("Проект с id=" + rq.projectId() + " не найдено")));
        projectMetricRepository.save(projectMetricEntity);
    }

    public List<ProjectMetricModel> getListByProjectId(Long projectId) {
        List<ProjectMetricModel> projectMetricModelList = projectMetricMapper.toModelList(projectMetricRepository.findAllByProjectId(projectId));
        for (ProjectMetricModel model: projectMetricModelList) {
            model.setProjectId(projectId);
        }
        return projectMetricModelList;
    }

    @Transactional
    public void updateList(List<ProjectMetricModel> rq) {
        List<ProjectMetricEntity> projectMetricEntityList = projectMetricMapper.toEntityList(rq);
        Long id = rq.getFirst().getProjectId();
        for (ProjectMetricEntity projectMetricEntity: projectMetricEntityList) {
            projectMetricEntity.setProject(projectRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Проект с id=" + id + " не найдено")));
        }
        projectMetricRepository.saveAll(projectMetricEntityList);
    }

    @Transactional
    public ProjectMetricModel update(Long id, ProjectMetricRq rq) {
        ProjectMetricEntity projectMetricEntity = projectMetricRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Метрика проекта с id=" + id + " не найдено"));
        projectMetricEntity.setName(rq.name());
        projectMetricEntity.setComplete(rq.isComplete());
        projectMetricEntity.setMain(rq.isMain());
        projectMetricEntity.setOrderIndex(rq.orderIndex());

        return projectMetricMapper.toModel(projectMetricEntity);
    }


    public void delete(Long id) {
        projectMetricRepository.deleteById(id);
    }
}
