package my.help.useful.ai;

import java.time.OffsetDateTime;

public record ChatSessionDto(
        String id,
        String title,
        OffsetDateTime createdAt,
        OffsetDateTime lastUsed
) {
    public static ChatSessionDto from(ChatSession s) {
        return new ChatSessionDto(s.getId(), s.getTitle(), s.getCreatedAt(), s.getLastUsed());
    }
}