CREATE TABLE tenants (
    id         UUID                     NOT NULL,
    name       VARCHAR(255)             NOT NULL,
    slug       VARCHAR(255)             NOT NULL,
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    CONSTRAINT pk_tenants PRIMARY KEY (id),
    CONSTRAINT uq_tenants_name UNIQUE (name),
    CONSTRAINT uq_tenants_slug UNIQUE (slug)
);

CREATE TABLE users (
    id            UUID                     NOT NULL,
    tenant_id     UUID                     NOT NULL,
    email         VARCHAR(255)             NOT NULL,
    password_hash VARCHAR(255)             NOT NULL,
    full_name     VARCHAR(255)             NOT NULL,
    role          VARCHAR(255)             NOT NULL,
    created_at    TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    CONSTRAINT pk_users PRIMARY KEY (id),
    CONSTRAINT fk_users_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    CONSTRAINT uq_users_tenant_email UNIQUE (tenant_id, email),
    CONSTRAINT ck_users_role CHECK (role IN ('ADMIN', 'AGENT', 'CUSTOMER'))
);

CREATE TABLE tickets (
    id                UUID                     NOT NULL,
    tenant_id         UUID                     NOT NULL,
    subject           VARCHAR(255)             NOT NULL,
    description       TEXT                     NOT NULL,
    status            VARCHAR(255)             NOT NULL,
    priority          VARCHAR(255)             NOT NULL,
    created_by_id     UUID                     NOT NULL,
    assigned_agent_id UUID,
    created_at        TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    updated_at        TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    CONSTRAINT pk_tickets PRIMARY KEY (id),
    CONSTRAINT fk_tickets_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    CONSTRAINT fk_tickets_created_by FOREIGN KEY (created_by_id) REFERENCES users (id),
    CONSTRAINT fk_tickets_assigned_agent FOREIGN KEY (assigned_agent_id) REFERENCES users (id),
    CONSTRAINT ck_tickets_status CHECK (status IN ('OPEN', 'IN_PROGRESS', 'RESOLVED', 'CLOSED')),
    CONSTRAINT ck_tickets_priority CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH', 'URGENT'))
);

CREATE TABLE comments (
    id         UUID                     NOT NULL,
    ticket_id  UUID                     NOT NULL,
    author_id  UUID                     NOT NULL,
    body       TEXT                     NOT NULL,
    internal   BOOLEAN                  NOT NULL,
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    CONSTRAINT pk_comments PRIMARY KEY (id),
    CONSTRAINT fk_comments_ticket FOREIGN KEY (ticket_id) REFERENCES tickets (id),
    CONSTRAINT fk_comments_author FOREIGN KEY (author_id) REFERENCES users (id)
);