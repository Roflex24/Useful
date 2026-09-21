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
    public void ensure(String id) {
        if (repo.existsById(id)) return;
        OffsetDateTime now = OffsetDateTime.now();
        repo.save(new ChatSession(id, "Новый чат", now, now));
    }

    @Transactional
    public void touch(String id) {
        repo.findById(id).ifPresent(s -> s.setLastUsed(OffsetDateTime.now()));
    }

    @Transactional
    public void rename(String id, String title) {
        if (title == null || title.isBlank()) return;
        repo.findById(id).ifPresent(s -> s.setTitle(title.trim()));
    }

    @Transactional
    public void autoTitle(String id, String firstMessage) {
        if (firstMessage == null) return;
        String title = firstMessage.strip();
        if (title.isEmpty()) return;
        if (title.length() > 60) title = title.substring(0, 60) + "…";
        final String finalTitle = title;
        repo.findById(id).ifPresent(s -> {
            if (s.getTitle() == null || s.getTitle().equals("Новый чат")) {
                s.setTitle(finalTitle);
            }
        });
    }

    @Transactional
    public void delete(String id) {
        repo.deleteById(id);
    }
}