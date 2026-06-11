package my.help.useful.kanban.project;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.help.useful.kanban.project.metric.ProjectMetricModel;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProjectMetricAggregator {

    /**
     * Создает базовый объект ProjectWithMetricModel из ProjectEntity
     */
    public ProjectModel buildBasicProjectWithMetric(ProjectEntity entity) {
        return new ProjectModel(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getCreateDate(),
                null, null,
                entity.isArchived(),
                entity.getArchiveDate()
        );
    }

    /**
     * Обрабатывает метрики проекта и заполняет результат
     */
    public void processMetrics(List<ProjectMetricModel> metrics, ProjectModel result) {
        if (metrics == null || metrics.isEmpty()) {
            result.setMainMetricCompletePercent(0);
            return;
        }

        int totalNonMainMetrics = 0;
        int completedNonMainMetrics = 0;

        for (ProjectMetricModel metric : metrics) {
            if (metric.isMain()) {
                result.setMainProjectMetric(metric);
            } else {
                totalNonMainMetrics++;
                if (metric.isComplete()) {
                    completedNonMainMetrics++;
                }
            }
        }

        int completionPercent = calculateCompletionPercent(completedNonMainMetrics, totalNonMainMetrics);
        result.setMainMetricCompletePercent(completionPercent);
    }

    /**
     * Полностью собирает объект ProjectWithMetricModel из сущности и метрик
     */
    public ProjectModel aggregateProjectWithMetrics(ProjectEntity projectEntity,
                                                    List<ProjectMetricModel> metrics) {
        ProjectModel result = buildBasicProjectWithMetric(projectEntity);
        processMetrics(metrics, result);
        return result;
    }

    /**
     * Вычисляет процент завершения
     */
    private int calculateCompletionPercent(int completed, int total) {
        if (total == 0) {
            return 0;
        }
        return (completed * 100) / total;
    }
}