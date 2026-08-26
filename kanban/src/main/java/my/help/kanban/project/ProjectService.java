package my.help.kanban.project;

import lombok.RequiredArgsConstructor;
import my.help.kanban.common.ResourceNotFoundException;
import my.help.kanban.project.metric.*;
import my.help.kanban.column.ColumnEntity;
import my.help.kanban.column.ColumnRepository;
import my.help.kanban.column.ColumnService;
import my.help.kanban.task.TaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
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
        projectEntity.setArchived(rq.isArchived());
        if (rq.isArchived()) {
            projectEntity.setArchiveDate(LocalDate.now());
        }
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
    public void archiveProject(Long id) {
        ProjectEntity project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Проект с id=" + id + " не найдено"));
        project.setArchived(true);
        project.setArchiveDate(LocalDate.now());
        projectRepository.save(project);
    }

    @Transactional
    public void unarchiveProject(Long id) {
        ProjectEntity project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Проект с id=" + id + " не найдено"));
        project.setArchived(false);
        project.setArchiveDate(null);
        projectRepository.save(project);
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
                .orElseThrow(() -> new ResourceNotFoundException("Проект с id=" + id + " не найдено"));

        List<ProjectMetricModel> projectMetricModelList = projectMetricMapper.toModelList(
                projectMetricRepository.findAllByProjectId(id)
        );

        return projectMetricAggregator.aggregateProjectWithMetrics(projectEntity, projectMetricModelList);
    }

    public List<ProjectModel> getProjectList() {
        List<ProjectEntity> projectEntityList = projectRepository.findAll();
        return buildProjectModelsWithMetrics(projectEntityList);
    }

    private List<ProjectModel> buildProjectModelsWithMetrics(List<ProjectEntity> projects) {
        List<ProjectModel> projectWithMetricModelList = new ArrayList<>();

        for (ProjectEntity projectEntity : projects) {
            projectWithMetricModelList.add(
                    projectMetricAggregator.aggregateProjectWithMetrics(
                            projectEntity, projectMetricMapper.toModelList(
                                    projectMetricRepository.findAllByProjectId(
                                            projectEntity.getId()))));
        }
        return projectWithMetricModelList;
    }
}