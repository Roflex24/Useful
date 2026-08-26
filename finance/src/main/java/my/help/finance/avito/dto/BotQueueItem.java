package my.help.finance.avito.dto;

public record BotQueueItem(
        Long id,
        String avitoId,
        String url,
        Integer previousAttempts
) {}