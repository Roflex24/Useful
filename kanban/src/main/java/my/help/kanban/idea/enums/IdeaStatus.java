package my.help.kanban.idea.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum IdeaStatus {
    NEW("new"),
    IN_PROGRESS("in-progress"),
    DONE("done"),
    ARCHIVED("archived");

    private final String value;

    IdeaStatus(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static IdeaStatus fromValue(String value) {
        for (IdeaStatus status : IdeaStatus.values()) {
            if (status.value.equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Неизвестный статус: " + value);
    }
}