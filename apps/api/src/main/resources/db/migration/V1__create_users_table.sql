CREATE TABLE packages
(
    id             UUID PRIMARY KEY        DEFAULT gen_random_uuid(),
    name           VARCHAR(100)   NOT NULL,
    max_servers    INTEGER        NOT NULL CHECK (max_servers >= 0),
    price          NUMERIC(10, 2) NOT NULL DEFAULT 0.00,
    currency       VARCHAR(3)     NOT NULL DEFAULT 'USD',
    billing_period VARCHAR(20)    NOT NULL DEFAULT 'FREE'
        CHECK (billing_period IN ('FREE', 'MONTHLY', 'YEARLY')),
    is_public      BOOLEAN        NOT NULL DEFAULT TRUE,
    is_active      BOOLEAN        NOT NULL DEFAULT TRUE,
    created_at     TIMESTAMPTZ    NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ    NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX uq_packages_name ON packages (name);
CREATE INDEX idx_packages_active ON packages (is_active);

CREATE TABLE roles
(
    id          UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    name        VARCHAR(50) NOT NULL,
    description TEXT,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX uq_roles_name ON roles (name);

INSERT INTO roles (name, description)
VALUES ('ADMIN', 'Full workspace ownership permissions'),
       ('MEMBER', 'Standard workspace member permissions');

CREATE TABLE permissions
(
    id          UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    name        VARCHAR(100) NOT NULL,
    description TEXT,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX uq_permissions_name ON permissions (name);

CREATE TABLE role_permissions
(
    role_id       UUID NOT NULL REFERENCES roles (id) ON DELETE CASCADE,
    permission_id UUID NOT NULL REFERENCES permissions (id) ON DELETE CASCADE,
    PRIMARY KEY (role_id, permission_id)
);

CREATE INDEX idx_role_permissions_permission_id ON role_permissions (permission_id);

-- status: 'ACTIVE' | 'DISABLED' | 'PENDING_VERIFICATION'
-- password_hash: nullable — admin tarafından oluşturulan hesaplar aktivasyon sonrası set eder
CREATE TABLE users
(
    id            UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    email         VARCHAR(255) NOT NULL,
    display_name  VARCHAR(100),
    password_hash VARCHAR(255),
    status        VARCHAR(30)  NOT NULL DEFAULT 'PENDING_VERIFICATION'
        CHECK (status IN ('ACTIVE', 'DISABLED', 'PENDING_VERIFICATION')),
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX uq_users_email ON users (email);
CREATE INDEX idx_users_status ON users (status);

CREATE TABLE workspaces
(
    id                 UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    name               VARCHAR(100) NOT NULL,
    slug               VARCHAR(100),
    created_by_user_id UUID         REFERENCES users (id) ON DELETE SET NULL,
    created_at         TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at         TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX uq_workspaces_slug ON workspaces (slug) WHERE slug IS NOT NULL;
CREATE INDEX idx_workspaces_created_by_user_id ON workspaces (created_by_user_id);

CREATE TABLE workspace_members
(
    workspace_id UUID        NOT NULL REFERENCES workspaces (id) ON DELETE CASCADE,
    user_id      UUID        NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    role_id      UUID        NOT NULL REFERENCES roles (id),
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (workspace_id, user_id)
);

CREATE INDEX idx_workspace_members_user_id ON workspace_members (user_id);
CREATE INDEX idx_workspace_members_role_id ON workspace_members (role_id);
