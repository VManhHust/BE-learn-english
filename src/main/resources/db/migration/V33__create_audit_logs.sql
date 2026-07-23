CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGSERIAL PRIMARY KEY,
    actor_user_id BIGINT,
    actor_email VARCHAR(255),
    action VARCHAR(20) NOT NULL,
    resource VARCHAR(120) NOT NULL,
    resource_id VARCHAR(120),
    http_method VARCHAR(10) NOT NULL,
    request_path VARCHAR(500) NOT NULL,
    query_string TEXT,
    response_status INTEGER,
    success BOOLEAN NOT NULL DEFAULT FALSE,
    ip_address VARCHAR(80),
    user_agent TEXT,
    details JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_audit_logs_created_at
    ON audit_logs(created_at DESC);

CREATE INDEX IF NOT EXISTS idx_audit_logs_actor_user_id
    ON audit_logs(actor_user_id);

CREATE INDEX IF NOT EXISTS idx_audit_logs_resource
    ON audit_logs(resource, resource_id);

CREATE INDEX IF NOT EXISTS idx_audit_logs_action
    ON audit_logs(action);
