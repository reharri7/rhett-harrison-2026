package com.rhettharrison.cms.platform.common.repository;

import com.rhettharrison.cms.platform.common.tenant.TenantAwareEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

/**
 * Base repository for tenant-aware entities.
 * All queries are automatically scoped by tenant_id via Hibernate filter.
 * No need to pass tenant_id as a parameter - it's enforced automatically.
 */
@NoRepositoryBean
public interface TenantAwareRepository<T extends TenantAwareEntity, ID> extends JpaRepository<T, ID> {
  // All standard JpaRepository methods (findAll, findById, save, etc.)
  // are automatically scoped to the current tenant via Hibernate filter
}
