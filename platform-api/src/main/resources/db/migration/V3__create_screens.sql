-- V3__create_screens.sql

CREATE TABLE screens (
                         id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                         tenant_id    UUID NOT NULL,
                         path         VARCHAR(255) NOT NULL,
                         type         VARCHAR(32) NOT NULL,
                         content      JSONB NOT NULL,
                         status       VARCHAR(16) NOT NULL,
                         created_at   TIMESTAMP NOT NULL DEFAULT NOW(),
                         updated_at   TIMESTAMP NOT NULL DEFAULT NOW(),

                         CONSTRAINT fk_screens_tenant
                             FOREIGN KEY (tenant_id)
                                 REFERENCES tenants(id),

                         CONSTRAINT uq_tenant_path
                             UNIQUE (tenant_id, path)
);
