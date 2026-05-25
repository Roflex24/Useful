package my.help.useful.kanban.project.metric;

import lombok.RequiredArgsConstructor;
import my.help.useful.kanban.project.ProjectRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectMetricService {

    private final ProjectMetricRepository projectMetricRepository;
    private final ProjectMetricMapper projectMetricMapper;
    private final ProjectRepository projectRepository;

    public void createProjectMetric(ProjectMetricRq rq) {
        ProjectMetricEntity projectMetricEntity = projectMetricMapper.projectMetricRqToEntity(rq);
        projectMetricEntity.setProject(projectRepository.findById(rq.getProjectId()).get());
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
            projectMetricEntity.setProject(projectRepository.findById(id).get());
        }
        projectMetricRepository.saveAll(projectMetricEntityList);
    }

    public void updateProjectMetric(ProjectMetricModel projectMetricModel) {
        ProjectMetricEntity projectMetricEntity = projectMetricMapper.toEntity(projectMetricModel);
        projectMetricEntity.setProject(projectRepository.findById(projectMetricModel.getProjectId()).get());
        projectMetricRepository.save(projectMetricEntity);
    }


    public void deleteProjectById(Long id) {
        projectMetricRepository.deleteById(id);
    }
}
