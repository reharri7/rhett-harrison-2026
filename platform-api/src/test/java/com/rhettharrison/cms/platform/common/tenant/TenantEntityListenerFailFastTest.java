package com.rhettharrison.cms.platform.common.tenant;

import static org.junit.jupiter.api.Assertions.assertThrows;

import com.rhettharrison.cms.platform.domain.testentity.TestTenantEntity;
import com.rhettharrison.cms.platform.domain.testentity.TestTenantEntityRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;

/**
 * Guardrails test: persisting a TenantAwareEntity without TenantContext should fail fast.
 * Uses H2 (no Flyway) and create-drop to avoid interacting with production schema.
 */
@SpringBootTest(properties = {
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.flyway.enabled=false",
    "spring.datasource.url=jdbc:h2:mem:tenantFailFast;DB_CLOSE_DELAY=-1;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH",
    "spring.datasource.driverClassName=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password="
})
@DirtiesContext
class TenantEntityListenerFailFastTest {

  @Autowired private TestTenantEntityRepository repository;

  @AfterEach
  void clearTenant() {
    TenantContext.clear();
  }

  @Test
  @Transactional
  void save_withoutTenantContext_shouldThrowIllegalStateException() {
    TestTenantEntity entity = new TestTenantEntity();
    entity.setName("no-tenant");

    // Expect the @PrePersist listener to fetch TenantContext and throw
    assertThrows(IllegalStateException.class, () -> {
      repository.saveAndFlush(entity);
    });
  }
}
