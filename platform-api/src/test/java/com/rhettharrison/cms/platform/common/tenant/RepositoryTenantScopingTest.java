package com.rhettharrison.cms.platform.common.tenant;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.rhettharrison.cms.platform.domain.testentity.TestTenantEntity;
import com.rhettharrison.cms.platform.domain.testentity.TestTenantEntityRepository;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.UUID;
import org.hibernate.Session;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(properties = {
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.flyway.enabled=false",
    "spring.datasource.url=jdbc:h2:mem:tenantTest;DB_CLOSE_DELAY=-1;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH",
    "spring.datasource.driverClassName=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password="
})
@DirtiesContext
@Transactional
class RepositoryTenantScopingTest {

  @Autowired private TestTenantEntityRepository repository;
  @Autowired private EntityManager entityManager;

  private UUID tenantA;
  private UUID tenantB;

  @BeforeEach
  void setUp() {
    tenantA = UUID.randomUUID();
    tenantB = UUID.randomUUID();
  }

  @AfterEach
  void tearDown() {
    // Clean tenant context and disable filter
    TenantContext.clear();
    Session session = entityManager.unwrap(Session.class);
    try {
      session.disableFilter("tenantFilter");
    } catch (IllegalArgumentException ignored) {
      // filter might not be enabled
    }
  }

  private void enableFilterFor(UUID tenantId) {
    TenantContext.clear();
    TenantContext.setTenantId(tenantId);
    Session session = entityManager.unwrap(Session.class);
    session.enableFilter("tenantFilter").setParameter("tenantId", tenantId);
  }

  @Test
  void prePersistListener_shouldSetTenantIdOnSave() {
    enableFilterFor(tenantA);

    TestTenantEntity e = new TestTenantEntity();
    e.setName("A1");
    TestTenantEntity saved = repository.saveAndFlush(e);

    assertEquals(tenantA, saved.getTenantId());
  }

  @Test
  void queries_shouldBeScopedByCurrentTenant() {
    // Save two for tenant A
    enableFilterFor(tenantA);
    repository.save(entity("A1"));
    repository.save(entity("A2"));
    repository.flush();

    // Save one for tenant B
    enableFilterFor(tenantB);
    repository.save(entity("B1"));
    repository.flush();

    // Query as tenant A
    enableFilterFor(tenantA);
    List<TestTenantEntity> aEntities = repository.findAll();
    assertEquals(2, aEntities.size());

    // Query as tenant B
    enableFilterFor(tenantB);
    List<TestTenantEntity> bEntities = repository.findAll();
    assertEquals(1, bEntities.size());
  }

  private TestTenantEntity entity(String name) {
    TestTenantEntity e = new TestTenantEntity();
    e.setName(name);
    return e;
  }
}
