package my.help.kanban.project;

import lombok.RequiredArgsConstructor;
import my.help.kanban.column.Column;
import my.help.kanban.common.ResourceNotFoundException;
import my.help.kanban.metric.*;
import my.help.kanban.column.ColumnRepository;
import my.help.kanban.column.ColumnService;
import my.help.kanban.metric.dto.ProjectMetricRs;
import my.help.kanban.metric.dto.ProjectMetricRq;
import my.help.kanban.project.dto.ProjectRs;
import my.help.kanban.project.dto.ProjectRq;
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

    @Transactional
    public ProjectRs create(ProjectRq rq) {
        Project project = projectMapper.projectRqToEntity(rq);
        Project saved = projectRepository.save(project);
        columnService.createColumns(saved);
        projectMetricService.create(
                new ProjectMetricRq("Метрика завершения проекта", false, true, null, saved.getId())
        );
        return projectMapper.toModel(saved);
    }

    @Transactional
    public ProjectRs update(Long id, ProjectRq rq) {
        Project project = projectRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Проект с id=" + id + " не найдено"));
        project.setName(rq.name());
        project.setDescription(rq.description());

        return projectMapper.toModel(project);
    }

    @Transactional
    public void archiveProject(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Проект с id=" + id + " не найдено"));
        project.setArchived(true);
        project.setArchiveDate(LocalDate.now());
        projectRepository.save(project);
    }

    @Transactional
    public void unarchiveProject(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Проект с id=" + id + " не найдено"));
        project.setArchived(false);
        project.setArchiveDate(null);
        projectRepository.save(project);
    }

    @Transactional
    public void delete(Long id) {
        for(Column column : columnRepository.findAllByProjectId(id)) {
            taskRepository.deleteByColumnId(column.getId());
        }
        columnRepository.deleteByProjectId(id);
        projectMetricRepository.deleteByProjectId(id);
        projectRepository.deleteById(id);
    }

    public ProjectRs getProjectById(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Проект с id=" + id + " не найдено"));

        List<ProjectMetricRs> projectMetricRsList = projectMetricMapper.toModelList(
                projectMetricRepository.findAllByProjectId(id)
        );

        return projectMetricAggregator.aggregateProjectWithMetrics(project, projectMetricRsList);
    }

    public List<ProjectRs> getList() {
        List<Project> projectList = projectRepository.findAll();
        return buildProjectModelsWithMetrics(projectList);
    }

    private List<ProjectRs> buildProjectModelsWithMetrics(List<Project> projects) {
        List<ProjectRs> projectWithMetricModelList = new ArrayList<>();

        for (Project project : projects) {
            projectWithMetricModelList.add(
                    projectMetricAggregator.aggregateProjectWithMetrics(
                            project, projectMetricMapper.toModelList(
                                    projectMetricRepository.findAllByProjectId(
                                            project.getId()))));
        }
        return projectWithMetricModelList;
    }
}