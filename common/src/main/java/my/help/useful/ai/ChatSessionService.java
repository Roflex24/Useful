package my.help.useful.ai;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ChatSessionService {

    private final ChatSessionRepository repo;

    public ChatSessionService(ChatSessionRepository repo) {
        this.repo = repo;
    }

    @Transactional(readOnly = true)
    public List<ChatSessionDto> findAll() {
        return repo.findAllByOrderByLastUsedDesc().stream()
                .map(ChatSessionDto::from)
                .toList();
    }

    @Transactional
    public ChatSessionDto create(String title) {
        String id = UUID.randomUUID().toString();
        OffsetDateTime now = OffsetDateTime.now();
        String t = (title == null || title.isBlank()) ? "Новый чат" : title.trim();
        ChatSession s = new ChatSession(id, t, now, now);
        return ChatSessionDto.from(repo.save(s));
    }

    @Transactional
    public void rename(String id, String title) {
        if (title == null || title.isBlank()) return;
        repo.findById(id).ifPresent(s -> s.setTitle(title.trim()));
    }

    @Transactional
    public void delete(String id) {
        repo.deleteById(id);
    }
}