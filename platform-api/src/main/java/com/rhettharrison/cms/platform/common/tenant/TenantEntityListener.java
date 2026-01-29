package com.rhettharrison.cms.platform.common.tenant;

import jakarta.persistence.PrePersist;

public class TenantEntityListener {

  @PrePersist
  public void setTenantId(Object entity) {
    if(entity instanceof TenantAwareEntity tenantAwareEntity) {
      tenantAwareEntity.setTenantId(TenantContext.getTenantId());
    }
  }
}
