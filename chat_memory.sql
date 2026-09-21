DROP TABLE IF EXISTS SPRING_AI_CHAT_MEMORY;

CREATE TABLE SPRING_AI_CHAT_MEMORY (
    conversation_id VARCHAR(36) NOT NULL,
    content         TEXT        NOT NULL,
    type            VARCHAR(10) NOT NULL,
    timestamp       TIMESTAMP WITH TIME ZONE NOT NULL,
    sequence_id     BIGINT      NOT NULL,
    PRIMARY KEY (conversation_id, sequence_id)
);

CREATE INDEX IF NOT EXISTS SPRING_AI_CHAT_MEMORY_CONVERSATION_ID_SEQUENCE_ID_IDX
    ON SPRING_AI_CHAT_MEMORY (conversation_id, sequence_id);