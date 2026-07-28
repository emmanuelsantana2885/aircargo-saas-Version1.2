CREATE SCHEMA IF NOT EXISTS notification;

CREATE TABLE IF NOT EXISTS notification (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    type VARCHAR(20) NOT NULL,
    title VARCHAR(255) NOT NULL,
    body TEXT,
    entity_type VARCHAR(50),
    entity_id UUID,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_notification_user_id ON notification(user_id);
CREATE INDEX IF NOT EXISTS idx_notification_user_unread ON notification(user_id, is_read);
CREATE INDEX IF NOT EXISTS idx_notification_created_at ON notification(created_at DESC);
