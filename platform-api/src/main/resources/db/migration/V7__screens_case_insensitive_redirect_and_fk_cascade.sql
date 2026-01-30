-- V7__screens_case_insensitive_redirect_and_fk_cascade.sql
-- 1) Add lifecycle and redirect columns
ALTER TABLE screens
    ADD COLUMN IF NOT EXISTS published_at TIMESTAMP NULL,
    ADD COLUMN IF NOT EXISTS redirect_target_url VARCHAR(2048),
    ADD COLUMN IF NOT EXISTS redirect_status INT;

-- 2) Replace unique constraint with case-insensitive unique index
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_name = 'uq_tenant_path'
          AND table_name = 'screens'
    ) THEN
        ALTER TABLE screens DROP CONSTRAINT uq_tenant_path;
    END IF;
END $$;

-- Create unique index on lower(path)
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_indexes 
        WHERE schemaname = 'public' AND indexname = 'uq_screens_tenant_lower_path'
    ) THEN
        CREATE UNIQUE INDEX uq_screens_tenant_lower_path ON screens(tenant_id, lower(path));
    END IF;
END $$;

-- 3) Ensure FK has ON DELETE CASCADE
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_name = 'fk_screens_tenant'
          AND table_name = 'screens'
    ) THEN
        ALTER TABLE screens DROP CONSTRAINT fk_screens_tenant;
    END IF;
    ALTER TABLE screens
        ADD CONSTRAINT fk_screens_tenant
        FOREIGN KEY (tenant_id)
        REFERENCES tenants(id)
        ON DELETE CASCADE;
END $$;
