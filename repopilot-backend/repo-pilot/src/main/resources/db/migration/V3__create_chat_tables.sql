CREATE SCHEMA IF NOT EXISTS chat;

--------------------------------------------------------------------------------------------------------
-- Table: chat.chat_sessions
--------------------------------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS chat.chat_sessions
(
    id            UUID                     NOT NULL,

    user_id       UUID                     NOT NULL,
    repository_id UUID                     NOT NULL,

    title         VARCHAR(200)             NOT NULL DEFAULT 'New chat',

    version       BIGINT                   NOT NULL DEFAULT 0,

    created_at    TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at    TIMESTAMP WITH TIME ZONE NOT NULL,

    created_by    UUID,
    updated_by    UUID,

    CONSTRAINT pk_chat_sessions
        PRIMARY KEY (id),

    CONSTRAINT fk_chat_sessions_user
        FOREIGN KEY (user_id)
            REFERENCES auth.users (user_id)
            ON DELETE CASCADE,

    CONSTRAINT fk_chat_sessions_repository
        FOREIGN KEY (repository_id)
            REFERENCES repo.repositories (repository_id)
            ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_chat_sessions_user_repo
    ON chat.chat_sessions (user_id, repository_id);

CREATE INDEX IF NOT EXISTS idx_chat_sessions_created_at
    ON chat.chat_sessions (created_at);

--------------------------------------------------------------------------------------------------------
-- Table: chat.chat_messages
--------------------------------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS chat.chat_messages
(
    id         UUID                     NOT NULL,

    session_id UUID                     NOT NULL,

    role       VARCHAR(20)              NOT NULL,
    content    TEXT                     NOT NULL,
    citations  TEXT,

    created_at TIMESTAMP WITH TIME ZONE NOT NULL,

    version    BIGINT                   NOT NULL DEFAULT 0,

    CONSTRAINT pk_chat_messages
        PRIMARY KEY (id),

    CONSTRAINT fk_chat_messages_session
        FOREIGN KEY (session_id)
            REFERENCES chat.chat_sessions (id)
            ON DELETE CASCADE,

    CONSTRAINT chk_chat_messages_role
        CHECK (role IN ('USER', 'ASSISTANT', 'SYSTEM'))
);

CREATE INDEX IF NOT EXISTS idx_chat_messages_session_id
    ON chat.chat_messages (session_id);

CREATE INDEX IF NOT EXISTS idx_chat_messages_created_at
    ON chat.chat_messages (created_at);

--------------------------------------------------------------------------------------------------------
-- Public schema fallback tables (if default public search_path is used without schema prefix)
--------------------------------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.chat_sessions
(
    id            UUID                     NOT NULL,

    user_id       UUID                     NOT NULL,
    repository_id UUID                     NOT NULL,

    title         VARCHAR(200)             NOT NULL DEFAULT 'New chat',

    version       BIGINT                   NOT NULL DEFAULT 0,

    created_at    TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at    TIMESTAMP WITH TIME ZONE NOT NULL,

    created_by    UUID,
    updated_by    UUID,

    CONSTRAINT pk_public_chat_sessions
        PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS public.chat_messages
(
    id         UUID                     NOT NULL,

    session_id UUID                     NOT NULL,

    role       VARCHAR(20)              NOT NULL,
    content    TEXT                     NOT NULL,
    citations  TEXT,

    created_at TIMESTAMP WITH TIME ZONE NOT NULL,

    version    BIGINT                   NOT NULL DEFAULT 0,

    CONSTRAINT pk_public_chat_messages
        PRIMARY KEY (id)
);
