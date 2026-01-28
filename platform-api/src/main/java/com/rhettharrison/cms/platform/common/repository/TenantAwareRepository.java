package com.rhettharrison.cms.platform.common.repository;

import com.rhettharrison.cms.platform.common.jpa.TenantAwareEntity;
import com.rhettharrison.cms.platform.common.tenant.TenantContext;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TenantAwareRepository<T extends TenantAwareEntity, ID> extends JpaRepository<T, ID> {
  default Optional<T> findByIdForTenant(ID id) {
    return findAllByTenantIdAndId(TenantContext.getTenantId(), id).stream().findFirst();
  }

  List<T> findAllByTenantId(String tenantId);

  List<T> findAllByTenantIdAndId(String tenantId, ID id);
}
