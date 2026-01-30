package com.rhettharrison.cms.platform.domain.model.screen;

import static org.junit.jupiter.api.Assertions.*;

import com.rhettharrison.cms.platform.common.tenant.TenantContext;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.util.Optional;
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
    "spring.datasource.url=jdbc:h2:mem:screenTest;DB_CLOSE_DELAY=-1;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH",
    "spring.datasource.driverClassName=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password="
})
@DirtiesContext
@Transactional
class ScreenLifecycleAndRepositoryTest {

  @Autowired private ScreenRepository screenRepository;
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
    TenantContext.clear();
    Session session = entityManager.unwrap(Session.class);
    try {
      session.disableFilter("tenantFilter");
    } catch (IllegalArgumentException ignored) {
    }
  }

  private void enableFilterFor(UUID tenantId) {
    TenantContext.clear();
    TenantContext.setTenantId(tenantId);
    Session session = entityManager.unwrap(Session.class);
    session.enableFilter("tenantFilter").setParameter("tenantId", tenantId);
  }

  private Screen newScreen(String path, ScreenStatus status) {
    Screen s = new Screen();
    s.setPath(path);
    s.setType(ScreenType.MARKDOWN);
    s.setStatus(status);
    s.setContent("{}");
    return s;
  }

  @Test
  void publishedAt_isSetOnCreateWhenPublished() {
    enableFilterFor(tenantA);
    Screen s = newScreen("/blog", ScreenStatus.PUBLISHED);
    s.setPublishedAt(null);
    Screen saved = screenRepository.saveAndFlush(s);

    assertNotNull(saved.getPublishedAt());
  }

  @Test
  void publishedAt_transitionsAreManagedByEntityCallbacks() throws InterruptedException {
    enableFilterFor(tenantA);
    Screen s = newScreen("/home", ScreenStatus.DRAFT);
    s = screenRepository.saveAndFlush(s);
    assertNull(s.getPublishedAt());

    // Transition to PUBLISHED should set publishedAt
    s.setStatus(ScreenStatus.PUBLISHED);
    s = screenRepository.saveAndFlush(s);
    Instant firstPublishedAt = s.getPublishedAt();
    assertNotNull(firstPublishedAt);

    // Transition back to DRAFT should clear publishedAt
    s.setStatus(ScreenStatus.DRAFT);
    s = screenRepository.saveAndFlush(s);
    assertNull(s.getPublishedAt());

    // Publish again should set a (new) publishedAt
    s.setStatus(ScreenStatus.PUBLISHED);
    Thread.sleep(5); // ensure timestamp can tick
    s = screenRepository.saveAndFlush(s);
    assertNotNull(s.getPublishedAt());
  }

  @Test
  void repository_findByPathAndStatus_scopedByTenant() {
    // Create for tenant A
    enableFilterFor(tenantA);
    screenRepository.saveAndFlush(newScreen("/about", ScreenStatus.PUBLISHED));

    // Create same path for tenant B as DRAFT
    enableFilterFor(tenantB);
    screenRepository.saveAndFlush(newScreen("/about", ScreenStatus.DRAFT));

    // Query as tenant A
    enableFilterFor(tenantA);
    Optional<Screen> aPub = screenRepository.findByPathAndStatus("/about", ScreenStatus.PUBLISHED);
    Optional<Screen> aDraft = screenRepository.findByPathAndStatus("/about", ScreenStatus.DRAFT);
    assertTrue(aPub.isPresent());
    assertTrue(aDraft.isEmpty());

    // Query as tenant B
    enableFilterFor(tenantB);
    Optional<Screen> bPub = screenRepository.findByPathAndStatus("/about", ScreenStatus.PUBLISHED);
    Optional<Screen> bDraft = screenRepository.findByPathAndStatus("/about", ScreenStatus.DRAFT);
    assertTrue(bPub.isEmpty());
    assertTrue(bDraft.isPresent());
  }
}
