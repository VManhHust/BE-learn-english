ALTER TABLE user_notifications
    DROP CONSTRAINT IF EXISTS ck_user_notification_type;

ALTER TABLE user_notifications
    ADD CONSTRAINT ck_user_notification_type CHECK (
        type IN (
            'VOCABULARY_REVIEW_DUE',
            'STREAK_REMINDER',
            'CONTINUE_LESSON',
            'NEW_VOCABULARY_DECK',
            'NEW_VOCABULARY_TOPIC',
            'NEW_LEARNING_TOPIC',
            'NEW_VIDEO_LESSON'
        )
    );
