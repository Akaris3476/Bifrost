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
    role_id       UUID         NOT NULL REFERENCES roles (id),
    status        VARCHAR(30)  NOT NULL DEFAULT 'PENDING_VERIFICATION'
        CHECK (status IN ('ACTIVE', 'DISABLED', 'PENDING_VERIFICATION')),
    package_id    UUID         REFERENCES packages (id) ON DELETE SET NULL,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX uq_users_email ON users (email);
CREATE INDEX idx_users_role_id ON users (role_id);
CREATE INDEX idx_users_package_id ON users (package_id);
CREATE INDEX idx_users_status ON users (status);
