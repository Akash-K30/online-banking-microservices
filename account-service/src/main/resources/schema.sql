CREATE TABLE IF NOT EXISTS accounts (
    id            UUID PRIMARY KEY,
    user_id       UUID NOT NULL,
    balance       NUMERIC(19,4) NOT NULL DEFAULT 0,
    held_amount   NUMERIC(19,4) NOT NULL DEFAULT 0,
    status        VARCHAR(20) NOT NULL,
    created_at    TIMESTAMP NOT NULL,
    version       BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_accounts_user_id ON accounts (user_id);

-- Append-only: no UPDATE or DELETE should ever be issued against this table.
CREATE TABLE IF NOT EXISTS ledger_entries (
    id               UUID PRIMARY KEY,
    account_id       UUID NOT NULL REFERENCES accounts (id),
    transaction_ref  VARCHAR(100),
    entry_type       VARCHAR(20) NOT NULL,
    amount           NUMERIC(19,4) NOT NULL,
    created_at       TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_ledger_account_id ON ledger_entries (account_id);
-- Powers the idempotency check (has this refId already been applied to this account?).
CREATE INDEX IF NOT EXISTS idx_ledger_account_ref ON ledger_entries (account_id, transaction_ref);

CREATE TABLE IF NOT EXISTS outbox_events (
    id            UUID PRIMARY KEY,
    aggregate_id  UUID NOT NULL,
    event_type    VARCHAR(100) NOT NULL,
    payload       JSONB NOT NULL,
    status        VARCHAR(20) NOT NULL,
    created_at    TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_outbox_status_created ON outbox_events (status, created_at);
