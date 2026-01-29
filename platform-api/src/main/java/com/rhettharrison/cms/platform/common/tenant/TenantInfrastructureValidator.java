package com.rhettharrison.cms.platform.common.tenant;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.hibernate.SessionFactory;
import org.hibernate.engine.spi.FilterDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import jakarta.persistence.EntityManagerFactory;

/**
 * Validates tenant infrastructure is properly configured at startup.
 * Fails fast if critical tenant components are missing.
 */
@Component
@RequiredArgsConstructor
public class TenantInfrastructureValidator {

  private static final Logger logger = LoggerFactory.getLogger(TenantInfrastructureValidator.class);

  private final EntityManagerFactory entityManagerFactory;

  @PostConstruct
  public void validateTenantInfrastructure() {
    logger.info("Validating tenant infrastructure...");

    validateHibernateFilterExists();

    logger.info("Tenant infrastructure validation complete ✓");
  }

  private void validateHibernateFilterExists() {
    SessionFactory sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);
    FilterDefinition filterDef = sessionFactory.getFilterDefinition("tenantFilter");

    if (filterDef == null) {
      throw new IllegalStateException(
          "Hibernate tenant filter 'tenantFilter' is not defined. " +
              "Ensure TenantAwareEntity is properly annotated with @FilterDef."
      );
    }

    if (!filterDef.getParameterNames().contains("tenantId")) {
      throw new IllegalStateException(
          "Hibernate tenant filter 'tenantFilter' does not have required 'tenantId' parameter."
      );
    }

    logger.info("✓ Hibernate tenant filter 'tenantFilter' is properly configured");
  }
}
