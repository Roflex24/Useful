package my.help.kanban.idea.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum IdeaPriority {
    HIGH("high"),
    MEDIUM("medium"),
    LOW("low");

    private final String value;

    IdeaPriority(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static IdeaPriority fromValue(String value) {
        for (IdeaPriority priority : IdeaPriority.values()) {
            if (priority.value.equalsIgnoreCase(value)) {
                return priority;
            }
        }
        throw new IllegalArgumentException("Неизвестный приоритет: " + value);
    }
}