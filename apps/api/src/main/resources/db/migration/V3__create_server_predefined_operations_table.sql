-- 'key': stable machine identifier (örn: 'RESTART_NGINX', 'DISK_CLEANUP')
-- operation_logs bu key'i denormalize olarak saklar — FK değil
CREATE TABLE server_predefined_operations
(
    id                    UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    key                   VARCHAR(100) NOT NULL,
    name                  VARCHAR(200) NOT NULL,
    description           TEXT,
    requires_confirmation BOOLEAN      NOT NULL DEFAULT FALSE,
    is_active             BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at            TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at            TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX uq_spo_key ON server_predefined_operations (key);
CREATE INDEX idx_spo_is_active ON server_predefined_operations (is_active);
