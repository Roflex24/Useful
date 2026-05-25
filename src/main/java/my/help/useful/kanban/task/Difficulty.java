package my.help.useful.kanban.task;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Difficulty {

    LOW(0, "Легкая"),
    MEDIUM(1, "Средняя"),
    HIGH(2, "Сложная"),
    URGENT(3, "Безумие"),
    BASE(4, "Непонятно");

    private final int value;
    private final String description;
}
