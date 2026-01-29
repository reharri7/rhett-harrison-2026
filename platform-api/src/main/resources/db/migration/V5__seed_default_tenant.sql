-- V5__seed_default_tenant.sql
-- Create a default tenant for local development

INSERT INTO tenants (id, slug, name, primary_domain)
VALUES (
    gen_random_uuid(),
    'default',
    'Default Tenant',
    'localhost'
);

-- Add localhost domain mapping for the default tenant
INSERT INTO tenant_domains (tenant_id, domain, is_primary)
SELECT id, 'localhost', true
FROM tenants
WHERE slug = 'default';
