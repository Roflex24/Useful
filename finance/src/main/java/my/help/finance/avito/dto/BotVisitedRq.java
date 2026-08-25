package my.help.finance.avito.dto;

/** Тело запроса, которым внешний бот отчитывается о результате визита. */
public record BotVisitedRq(
        boolean success,
        String html
) {}