package com.rhettharrison.cms.platform.common.tenant;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TenantContextTest {

  @AfterEach
  void cleanUp() {
    TenantContext.clear();
  }

  @Test
  void setTenantId_shouldSetTenantIdSuccessfully() {
    UUID tenantId = UUID.randomUUID();

    TenantContext.setTenantId(tenantId);

    assertEquals(tenantId, TenantContext.getTenantId());
  }

  @Test
  void setTenantId_shouldThrowExceptionWhenNull() {
    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> TenantContext.setTenantId(null)
    );

    assertEquals("Tenant ID cannot be null", exception.getMessage());
  }

  @Test
  void setTenantId_shouldThrowExceptionWhenAlreadySet() {
    UUID firstTenantId = UUID.randomUUID();
    UUID secondTenantId = UUID.randomUUID();

    TenantContext.setTenantId(firstTenantId);

    IllegalStateException exception = assertThrows(
        IllegalStateException.class,
        () -> TenantContext.setTenantId(secondTenantId)
    );

    assertTrue(exception.getMessage().contains("already set"));
    // Original value should remain
    assertEquals(firstTenantId, TenantContext.getTenantId());
  }

  @Test
  void getTenantId_shouldThrowExceptionWhenNotSet() {
    IllegalStateException exception = assertThrows(
        IllegalStateException.class,
        TenantContext::getTenantId
    );

    assertTrue(exception.getMessage().contains("No tenant context available"));
  }

  @Test
  void getTenantIdOrNull_shouldReturnNullWhenNotSet() {
    assertNull(TenantContext.getTenantIdOrNull());
  }

  @Test
  void getTenantIdOrNull_shouldReturnTenantIdWhenSet() {
    UUID tenantId = UUID.randomUUID();
    TenantContext.setTenantId(tenantId);

    assertEquals(tenantId, TenantContext.getTenantIdOrNull());
  }

  @Test
  void clear_shouldRemoveTenantId() {
    UUID tenantId = UUID.randomUUID();
    TenantContext.setTenantId(tenantId);

    TenantContext.clear();

    assertNull(TenantContext.getTenantIdOrNull());
  }

  @Test
  void clear_shouldAllowSettingNewTenantIdAfterClear() {
    UUID firstTenantId = UUID.randomUUID();
    UUID secondTenantId = UUID.randomUUID();

    TenantContext.setTenantId(firstTenantId);
    TenantContext.clear();
    TenantContext.setTenantId(secondTenantId);

    assertEquals(secondTenantId, TenantContext.getTenantId());
  }
}
