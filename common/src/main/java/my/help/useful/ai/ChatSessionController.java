package my.help.useful.ai;

import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/chat/sessions")
public class ChatSessionController {

    private final ChatSessionService sessions;
    private final ChatMemory chatMemory;

    public ChatSessionController(ChatSessionService sessions, ChatMemory chatMemory) {
        this.sessions = sessions;
        this.chatMemory = chatMemory;
    }

    @GetMapping
    public List<ChatSessionDto> list() {
        return sessions.findAll();
    }

    @PostMapping
    public ChatSessionDto create(@RequestBody(required = false) CreateSessionRequest req) {
        return sessions.create(req != null ? req.title() : null);
    }

    @PatchMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void rename(@PathVariable String id, @RequestBody RenameRequest req) {
        sessions.rename(id, req.title());
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        chatMemory.clear(id);
        sessions.delete(id);
    }

    record CreateSessionRequest(String title) {}
    record RenameRequest(String title) {}
}