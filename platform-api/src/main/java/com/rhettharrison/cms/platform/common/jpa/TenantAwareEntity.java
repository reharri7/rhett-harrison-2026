package com.rhettharrison.cms.platform.common.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;

@MappedSuperclass
@EntityListeners(TenantEntityListener.class)
public abstract class TenantAwareEntity {
  @Column(name = "tenant_id", nullable = false, updatable = false)
  protected String tenantId;

  public String getTenantId() {
    return tenantId;
  }

  protected void setTenantId(String tenantId) {
    this.tenantId = tenantId;
  }
}