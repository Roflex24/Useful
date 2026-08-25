package my.help.finance.avito.dto;

/** Один пункт очереди для бота-обходчика. */
public record BotQueueItem(
        Long id,
        String avitoId,
        String url,
        Integer previousAttempts
) {}