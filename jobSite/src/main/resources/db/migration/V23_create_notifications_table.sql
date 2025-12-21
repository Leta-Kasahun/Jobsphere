CREATE TABLE notifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    is_read BOOLEAN DEFAULT false,
    created_at TIMESTAMPTZ DEFAULT now()
);

CREATE INDEX idx_notifications_user_id ON notifications(user_id);
CREATE INDEX idx_notifications_created_at ON notifications(created_at DESC);
CREATE INDEX idx_notifications_is_read ON notifications(is_read);

COMMENT ON TABLE notifications IS 'In-app notifications for users';
COMMENT ON COLUMN notifications.title IS 'Notification title';
COMMENT ON COLUMN notifications.message IS 'Notification message content';
COMMENT ON COLUMN notifications.is_read IS 'Whether the notification has been read by the user';
