-- V4__create_tenant_domains.sql

CREATE TABLE tenant_domains (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id    UUID NOT NULL,
    domain       VARCHAR(255) NOT NULL UNIQUE,
    is_primary   BOOLEAN NOT NULL DEFAULT false,
    created_at   TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_tenant_domains_tenant
        FOREIGN KEY (tenant_id)
            REFERENCES tenants(id)
            ON DELETE CASCADE
);

-- Index for fast tenant lookup by domain
CREATE INDEX idx_tenant_domains_domain ON tenant_domains(domain);

-- Ensure each tenant has at most one primary domain
CREATE UNIQUE INDEX idx_tenant_domains_primary 
    ON tenant_domains(tenant_id) 
    WHERE is_primary = true;
