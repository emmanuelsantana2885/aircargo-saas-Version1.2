CREATE SCHEMA IF NOT EXISTS notification;

CREATE TABLE IF NOT EXISTS notification.audit_log (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID,
    email VARCHAR(255),
    full_name VARCHAR(255),
    action VARCHAR(50) NOT NULL,
    entity_type VARCHAR(50),
    entity_id VARCHAR(50),
    details TEXT,
    ip_address VARCHAR(64),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_audit_log_action ON notification.audit_log(action);
CREATE INDEX IF NOT EXISTS idx_audit_log_created_at ON notification.audit_log(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_audit_log_entity ON notification.audit_log(entity_type, entity_id);
