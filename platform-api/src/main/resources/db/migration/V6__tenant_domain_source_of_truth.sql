-- V6__tenant_domain_source_of_truth.sql
-- Remove primary_domain column from tenants; TenantDomain is the single source of truth

ALTER TABLE tenants
    DROP COLUMN IF EXISTS primary_domain;
