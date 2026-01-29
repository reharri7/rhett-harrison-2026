package com.rhettharrison.cms.platform.common.tenant;

import java.util.UUID;

public class TenantContext {

  private static final ThreadLocal<UUID> CURRENT_TENANT = new ThreadLocal<>();

  public static void setTenantId(UUID tenantId) {
    if (tenantId == null) {
      throw new IllegalArgumentException("Tenant ID cannot be null");
    }

    UUID existing = CURRENT_TENANT.get();
    if (existing != null) {
      throw new IllegalStateException(
          "Tenant ID already set for this request. Cannot override tenant context."
      );
    }

    CURRENT_TENANT.set(tenantId);
  }

  public static UUID getTenantId() {
    UUID tenantId = CURRENT_TENANT.get();
    if (tenantId == null) {
      throw new IllegalStateException(
          "No tenant context available. Ensure TenantResolutionFilter has executed."
      );
    }
    return tenantId;
  }

  public static UUID getTenantIdOrNull() {
    return CURRENT_TENANT.get();
  }

  public static void clear() {
    CURRENT_TENANT.remove();
  }
}
