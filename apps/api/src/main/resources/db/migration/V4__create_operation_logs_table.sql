-- operation_key: denormalize — audit trail katalog değişikliğinden etkilenmesin
-- user_id: SET NULL → kullanıcı silinse bile log kaydı korunsun
-- status: 'PENDING' | 'RUNNING' | 'SUCCESS' | 'FAILED'
CREATE TABLE operation_logs
(
    id            UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    server_id     UUID         NOT NULL REFERENCES servers (id) ON DELETE CASCADE,
    user_id       UUID         REFERENCES users (id) ON DELETE SET NULL,
    operation_key VARCHAR(100) NOT NULL,
    status        VARCHAR(20)  NOT NULL DEFAULT 'PENDING'
        CHECK (status IN ('PENDING', 'RUNNING', 'SUCCESS', 'FAILED')),
    output        TEXT,
    error_message TEXT,
    initiated_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    completed_at  TIMESTAMPTZ,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_oplogs_server_id ON operation_logs (server_id);
CREATE INDEX idx_oplogs_user_id ON operation_logs (user_id);
CREATE INDEX idx_oplogs_status ON operation_logs (status);
CREATE INDEX idx_oplogs_operation_key ON operation_logs (operation_key);
CREATE INDEX idx_oplogs_initiated_at ON operation_logs (initiated_at DESC);
