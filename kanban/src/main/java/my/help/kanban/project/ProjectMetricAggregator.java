package my.help.kanban.project;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.help.kanban.metric.dto.ProjectMetricRs;
import my.help.kanban.project.dto.ProjectRs;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProjectMetricAggregator {

    /**
     * Создает базовый объект ProjectWithMetricModel из ProjectEntity
     */
    public ProjectRs buildBasicProjectWithMetric(Project entity) {
        return new ProjectRs(
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
    public void processMetrics(List<ProjectMetricRs> metrics, ProjectRs result) {
        if (metrics == null || metrics.isEmpty()) {
            result.setMainMetricCompletePercent(0);
            return;
        }

        int totalNonMainMetrics = 0;
        int completedNonMainMetrics = 0;

        for (ProjectMetricRs metric : metrics) {
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
    public ProjectRs aggregateProjectWithMetrics(Project project,
                                                 List<ProjectMetricRs> metrics) {
        ProjectRs result = buildBasicProjectWithMetric(project);
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