CREATE TABLE IF NOT EXISTS notification_log (
    id          UUID PRIMARY KEY,
    user_id     UUID NOT NULL,
    channel     VARCHAR(20) NOT NULL,
    template    VARCHAR(100) NOT NULL,
    status      VARCHAR(20) NOT NULL,
    created_at  TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_notification_log_user_id ON notification_log (user_id);

-- Local read cache of contact details, kept current via user.events
-- (UserRegistered / KycUpdated). See UserContact.java for why this
-- service keeps its own copy instead of calling User Service directly.
CREATE TABLE IF NOT EXISTS user_contact (
    user_id     UUID PRIMARY KEY,
    email       VARCHAR(255),
    phone       VARCHAR(20),
    updated_at  TIMESTAMP NOT NULL
);
