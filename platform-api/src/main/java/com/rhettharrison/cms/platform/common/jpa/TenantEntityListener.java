package com.rhettharrison.cms.platform.common.jpa;

import com.rhettharrison.cms.platform.common.tenant.TenantContext;
import jakarta.persistence.PrePersist;

public class TenantEntityListener {

  @PrePersist
  public void setTenantId(Object entity) {
    if(entity instanceof TenantAwareEntity tenantAwareEntity) {
      tenantAwareEntity.setTenantId(TenantContext.getTenantId());
    }
  }
}
