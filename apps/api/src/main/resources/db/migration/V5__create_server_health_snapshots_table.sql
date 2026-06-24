-- Append-only tablo — satırlar güncellenmez (updated_at yok)
-- captured_at: Probe'un raporladığı zaman (insert zamanı değil)
-- UNIQUE(server_id, captured_at): Probe retry'larında duplicate önler
CREATE TABLE server_health_snapshots
(
    id             UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    server_id      UUID        NOT NULL REFERENCES servers (id) ON DELETE CASCADE,
    cpu_usage_pct  NUMERIC(5, 2) CHECK (cpu_usage_pct BETWEEN 0 AND 100),
    mem_used_mb    BIGINT,
    mem_total_mb   BIGINT,
    disk_used_gb   NUMERIC(12, 3),
    disk_total_gb  NUMERIC(12, 3),
    uptime_seconds BIGINT,
    load_1m        NUMERIC(8, 4),
    load_5m        NUMERIC(8, 4),
    load_15m       NUMERIC(8, 4),
    captured_at    TIMESTAMPTZ NOT NULL,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_health_server_captured ON server_health_snapshots (server_id, captured_at DESC);
CREATE UNIQUE INDEX uq_health_server_captured ON server_health_snapshots (server_id, captured_at);
