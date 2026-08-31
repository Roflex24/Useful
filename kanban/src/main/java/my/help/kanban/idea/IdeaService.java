package my.help.kanban.idea;

import lombok.RequiredArgsConstructor;
import my.help.kanban.common.ResourceNotFoundException;
import my.help.kanban.idea.dto.IdeaRq;
import my.help.kanban.idea.dto.IdeaRs;
import my.help.kanban.idea.enums.IdeaPriority;
import my.help.kanban.idea.enums.IdeaStatus;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class IdeaService {

    private final IdeaRepository ideaRepository;
    private final IdeaMapper ideaMapper;

    public List<IdeaRs> getIdeas(String status, String priority, String search, String sortDirection) {
        Specification<Idea> spec = null;

        if (status != null && !status.isEmpty()) {
            spec = IdeaSpecifications.withStatus(IdeaStatus.fromValue(status));
        }
        if (priority != null && !priority.isEmpty()) {
            Specification<Idea> prioritySpec = IdeaSpecifications.withPriority(IdeaPriority.fromValue(priority));
            spec = (spec == null) ? prioritySpec : spec.and(prioritySpec);
        }
        if (search != null && !search.isEmpty()) {
            Specification<Idea> searchSpec = IdeaSpecifications.withSearch(search);
            spec = (spec == null) ? searchSpec : spec.and(searchSpec);
        }

        Sort sort = Sort.by("createdAt");
        sort = "asc".equalsIgnoreCase(sortDirection) ? sort.ascending() : sort.descending();

        List<Idea> ideas;
        if (spec == null) {
            ideas = ideaRepository.findAll(sort);
        } else {
            ideas = ideaRepository.findAll(spec, sort);
        }

        return ideas.stream()
                .map(ideaMapper::toRs)
                .collect(Collectors.toList());
    }

    public IdeaRs getIdea(Long id) {
        Idea idea = ideaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Идея с id=" + id + " не найдена"));
        return ideaMapper.toRs(idea);
    }

    @Transactional
    public IdeaRs createIdea(IdeaRq ideaRq) {
        Idea idea = ideaMapper.toEntity(ideaRq);
        Idea saved = ideaRepository.save(idea);
        return ideaMapper.toRs(saved);
    }

    @Transactional
    public IdeaRs updateIdea(Long id, IdeaRq ideaRq) {
        Idea existing = ideaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Идея с id=" + id + " не найдена"));
        ideaMapper.updateEntityFromDto(ideaRq, existing);
        return ideaMapper.toRs(existing);
    }

    @Transactional
    public void deleteIdea(Long id) {
        if (!ideaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Идея с id=" + id + " не найдена");
        }
        ideaRepository.deleteById(id);
    }

    @Transactional
    public IdeaRs updateStatus(Long id, IdeaStatus status) {
        Idea idea = ideaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Идея с id=" + id + " не найдена"));
        idea.setStatus(status);
        return ideaMapper.toRs(idea);
    }
}