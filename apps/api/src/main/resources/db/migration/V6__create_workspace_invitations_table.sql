-- status: 'PENDING' | 'ACCEPTED' | 'EXPIRED' | 'REVOKED'
-- token_hash: raw invitation token is returned once and never stored in plaintext
CREATE TABLE workspace_invitations
(
    id                 UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    workspace_id       UUID         NOT NULL REFERENCES workspaces (id) ON DELETE CASCADE,
    email              VARCHAR(255) NOT NULL,
    role_id            UUID         NOT NULL REFERENCES roles (id),
    invited_by_user_id UUID         REFERENCES users (id) ON DELETE SET NULL,
    token_hash         VARCHAR(64)  NOT NULL,
    status             VARCHAR(20)  NOT NULL DEFAULT 'PENDING'
        CHECK (status IN ('PENDING', 'ACCEPTED', 'EXPIRED', 'REVOKED')),
    expires_at         TIMESTAMPTZ  NOT NULL,
    accepted_at        TIMESTAMPTZ,
    created_at         TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at         TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX uq_workspace_invitations_token_hash ON workspace_invitations (token_hash);
CREATE UNIQUE INDEX uq_workspace_invitations_pending_email
    ON workspace_invitations (workspace_id, email)
    WHERE status = 'PENDING';
CREATE INDEX idx_workspace_invitations_workspace_id ON workspace_invitations (workspace_id);
CREATE INDEX idx_workspace_invitations_email ON workspace_invitations (email);
CREATE INDEX idx_workspace_invitations_status ON workspace_invitations (status);
CREATE INDEX idx_workspace_invitations_expires_at ON workspace_invitations (expires_at);
