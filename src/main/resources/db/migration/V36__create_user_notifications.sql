CREATE TABLE user_notifications (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    type        VARCHAR(50)  NOT NULL,
    priority    VARCHAR(20)  NOT NULL DEFAULT 'NORMAL',
    data        JSONB        NOT NULL DEFAULT '{}'::jsonb,
    action_url  TEXT,
    dedupe_key  VARCHAR(200) NOT NULL,
    read_at     TIMESTAMPTZ,
    expires_at  TIMESTAMPTZ,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_user_notification_dedupe UNIQUE (user_id, dedupe_key),
    CONSTRAINT ck_user_notification_type CHECK (
        type IN ('VOCABULARY_REVIEW_DUE', 'STREAK_REMINDER', 'CONTINUE_LESSON')
    ),
    CONSTRAINT ck_user_notification_priority CHECK (
        priority IN ('NORMAL', 'HIGH')
    )
);

CREATE INDEX idx_user_notifications_user_created
    ON user_notifications(user_id, created_at DESC);

CREATE INDEX idx_user_notifications_user_unread
    ON user_notifications(user_id, created_at DESC)
    WHERE read_at IS NULL;

COMMENT ON TABLE user_notifications IS 'Persistent in-app learning notifications for each user';
COMMENT ON COLUMN user_notifications.data IS 'Language-neutral metadata rendered by the frontend';
COMMENT ON COLUMN user_notifications.dedupe_key IS 'Prevents duplicate reminders for the same user and period';
