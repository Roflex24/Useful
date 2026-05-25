package my.help.useful.kanban.project;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import my.help.useful.kanban.column.ColumnEntity;
import my.help.useful.kanban.column.ColumnRepository;
import my.help.useful.kanban.column.ColumnService;
import my.help.useful.kanban.project.metric.*;
import my.help.useful.kanban.task.TaskRepository;
import my.help.useful.kanban.task.TaskService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ColumnService columnService;
    private final ProjectRepository projectRepository;
    private final ProjectMapper projectMapper;
    private final ColumnRepository columnRepository;
    private final TaskRepository taskRepository;
    private final ProjectMetricRepository projectMetricRepository;
    private final ProjectMetricMapper projectMetricMapper;
    private final ProjectMetricAggregator projectMetricAggregator;
    private final ProjectMetricService projectMetricService;


    public ProjectModel createProject(ProjectRq rq) {
        ProjectEntity projectEntity = projectMapper.projectRqToEntity(rq);
        ProjectEntity saved = projectRepository.save(projectEntity);
        columnService.createColumns(saved);
        projectMetricService.createProjectMetric(
                new ProjectMetricRq("Метрика завершения проекта", false, true, null, projectEntity.getId())
        );
        return projectMapper.toModel(saved);
    }

    public void updateProject(ProjectModel rq) {
        projectRepository.save(projectMapper.toEntity(rq));
    }

    @Transactional
    public void deleteProjectById(Long id) {
        for(ColumnEntity columnEntity: columnRepository.findAllByProjectId(id)) {
            taskRepository.deleteByColumnId(columnEntity.getId());
        }
        columnRepository.deleteByProjectId(id);
        projectMetricRepository.deleteByProjectId(id);
        projectRepository.deleteById(id);
    }

    public ProjectModel getProjectById(Long id) {
        ProjectEntity projectEntity = projectRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Project not found with id: " + id));

        List<ProjectMetricModel> projectMetricModelList = projectMetricMapper.toModelList(
                projectMetricRepository.findAllByProjectId(id)
        );

        return projectMetricAggregator.aggregateProjectWithMetrics(projectEntity, projectMetricModelList);
    }

    // NOTE: Это решение делает N+1 запрос. Подходит только для небольшого количества проектов.
    public List<ProjectModel> getProjectList() {
        List<ProjectEntity> projectEntityList = projectRepository.findAll();

        List<ProjectModel> projectWithMetricModelList = new ArrayList<>();

        for (ProjectEntity projectEntity: projectEntityList) {
            projectWithMetricModelList.add(
                    projectMetricAggregator.aggregateProjectWithMetrics(
                            projectEntity, projectMetricMapper.toModelList(
                    projectMetricRepository.findAllByProjectId(
                            projectEntity.getId()))));
        }
        return projectWithMetricModelList;
    }
}
