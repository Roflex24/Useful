package my.help.useful.ai;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

@Entity
@Table(name = "chat_session")
public class ChatSession {

    @Id
    @Column(length = 36, nullable = false)
    private String id;

    @Column(length = 200, nullable = false)
    private String title;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "last_used", nullable = false)
    private OffsetDateTime lastUsed;

    public ChatSession() {}

    public ChatSession(String id, String title, OffsetDateTime createdAt, OffsetDateTime lastUsed) {
        this.id = id;
        this.title = title;
        this.createdAt = createdAt;
        this.lastUsed = lastUsed;
    }

    // getters/setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    public OffsetDateTime getLastUsed() { return lastUsed; }
    public void setLastUsed(OffsetDateTime lastUsed) { this.lastUsed = lastUsed; }
}