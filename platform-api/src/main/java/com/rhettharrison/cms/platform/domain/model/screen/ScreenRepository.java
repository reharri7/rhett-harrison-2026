package com.rhettharrison.cms.platform.domain.model.screen;

import com.rhettharrison.cms.platform.common.repository.TenantAwareRepository;
import java.util.Optional;
import java.util.UUID;

public interface ScreenRepository extends TenantAwareRepository<Screen, UUID> {
  // Automatically scoped by tenant via Hibernate filter
  Optional<Screen> findByPath(String path);
}
