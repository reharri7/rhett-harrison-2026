-- V2__create_tenants.sql

CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE tenants (
                         id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                         slug            VARCHAR(64) NOT NULL UNIQUE,
                         name            VARCHAR(255) NOT NULL,
                         primary_domain  VARCHAR(255),
                         created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

