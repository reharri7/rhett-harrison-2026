package com.rhettharrison.cms.platform.domain.model.user;

import com.rhettharrison.cms.platform.common.repository.TenantAwareRepository;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends TenantAwareRepository<User, UUID> {
  Optional<User> findByUsernameIgnoreCase(String username);

  // Explicit, tenant-keyed lookup for contexts where the Hibernate tenant filter
  // is not enabled (e.g., application startup seeders)
  Optional<User> findByTenantIdAndUsernameIgnoreCase(UUID tenantId, String username);
}
