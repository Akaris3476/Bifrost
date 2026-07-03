-- id: client'ta raw olarak tasinir, DB'de UUID tutulur
-- session_hash: cookie'den gelen session id SHA-256 ile hash'lenir; raw deger DB'ye yazilmaz
-- status bilgisi revoked_at / expires_at timestamp'lerinden anlasilir
CREATE TABLE auth_sessions
(
    id           UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    user_id      UUID        NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    session_hash VARCHAR(64) NOT NULL,
    ip_address   VARCHAR(64),
    user_agent   VARCHAR(255),
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    last_seen_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    expires_at   TIMESTAMPTZ NOT NULL,
    revoked_at   TIMESTAMPTZ
);

CREATE UNIQUE INDEX uq_auth_sessions_session_hash ON auth_sessions (session_hash);
CREATE INDEX idx_auth_sessions_user_id ON auth_sessions (user_id);
CREATE INDEX idx_auth_sessions_expires_at ON auth_sessions (expires_at);
