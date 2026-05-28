package my.help.useful.kanban.planning.entity;

public enum PlanStatus {
    EXCEEDED,      // закончилось выше ожидаемого
    AS_PLANNED,    // как планировал
    HARDER,        // оказалось сложнее
    NOT_STARTED,   // не смог приступить
    IN_PROGRESS    // в процессе
}