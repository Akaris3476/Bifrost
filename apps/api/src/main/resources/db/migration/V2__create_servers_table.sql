-- status: 'ONLINE' | 'OFFLINE' | 'UNKNOWN'
-- probe_secret: Probe (Rust) servisinin push isteğini doğrulamak için shared secret (BCrypt hash)
CREATE TABLE servers
(
    id           UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    user_id      UUID         NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    name         VARCHAR(100) NOT NULL,
    hostname     VARCHAR(253) NOT NULL,
    port         INTEGER      NOT NULL DEFAULT 22 CHECK (port BETWEEN 1 AND 65535),
    status       VARCHAR(20)  NOT NULL DEFAULT 'UNKNOWN'
        CHECK (status IN ('ONLINE', 'OFFLINE', 'UNKNOWN')),
    probe_secret VARCHAR(255) NOT NULL,
    last_seen_at TIMESTAMPTZ,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX uq_servers_user_hostname_port ON servers (user_id, hostname, port);
CREATE INDEX idx_servers_user_id ON servers (user_id);
CREATE INDEX idx_servers_status ON servers (status);
CREATE INDEX idx_servers_last_seen ON servers (last_seen_at);
