ALTER TABLE learning_topic
    ADD COLUMN IF NOT EXISTS status VARCHAR(30) NOT NULL DEFAULT 'PUBLISHED';

ALTER TABLE learning_exercise
    ADD COLUMN IF NOT EXISTS status VARCHAR(30) NOT NULL DEFAULT 'PUBLISHED';

ALTER TABLE vocabulary_topic
    ADD COLUMN IF NOT EXISTS status VARCHAR(30) NOT NULL DEFAULT 'PUBLISHED';

ALTER TABLE vocabulary_word
    ADD COLUMN IF NOT EXISTS status VARCHAR(30) NOT NULL DEFAULT 'PUBLISHED';

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'chk_learning_topic_status'
    ) THEN
        ALTER TABLE learning_topic
            ADD CONSTRAINT chk_learning_topic_status
                CHECK (status IN ('DRAFT', 'PUBLISHED', 'ARCHIVED'));
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'chk_learning_exercise_status'
    ) THEN
        ALTER TABLE learning_exercise
            ADD CONSTRAINT chk_learning_exercise_status
                CHECK (status IN ('DRAFT', 'PUBLISHED', 'ARCHIVED'));
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'chk_vocabulary_deck_status'
    ) THEN
        ALTER TABLE vocabulary_deck
            ADD CONSTRAINT chk_vocabulary_deck_status
                CHECK (status IN ('DRAFT', 'PUBLISHED', 'ARCHIVED'));
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'chk_vocabulary_topic_status'
    ) THEN
        ALTER TABLE vocabulary_topic
            ADD CONSTRAINT chk_vocabulary_topic_status
                CHECK (status IN ('DRAFT', 'PUBLISHED', 'ARCHIVED'));
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'chk_vocabulary_word_status'
    ) THEN
        ALTER TABLE vocabulary_word
            ADD CONSTRAINT chk_vocabulary_word_status
                CHECK (status IN ('DRAFT', 'PUBLISHED', 'ARCHIVED'));
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_learning_topic_status ON learning_topic(status);
CREATE INDEX IF NOT EXISTS idx_learning_exercise_status_topic ON learning_exercise(status, topic_id);
CREATE INDEX IF NOT EXISTS idx_vocabulary_topic_status_deck ON vocabulary_topic(status, deck_id);
CREATE INDEX IF NOT EXISTS idx_vocabulary_word_status_topic ON vocabulary_word(status, topic_id);
