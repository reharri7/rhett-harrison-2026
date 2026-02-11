package com.rhettharrison.cms.platform.common.tenant;

import jakarta.annotation.PostConstruct;
import org.hibernate.SessionFactory;
import org.hibernate.engine.spi.FilterDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import jakarta.persistence.EntityManagerFactory;
import com.rhettharrison.cms.platform.web.filter.RequestIdFilter;
import com.rhettharrison.cms.platform.web.filter.TenantResolutionFilter;

/**
 * Validates tenant infrastructure is properly configured at startup.
 * Fails fast if critical tenant components are missing.
 */
@Component
public class TenantInfrastructureValidator {

  private static final Logger logger = LoggerFactory.getLogger(TenantInfrastructureValidator.class);

  private final EntityManagerFactory entityManagerFactory;
  private final ApplicationContext applicationContext;

  // Explicit constructor to ensure final fields are initialized without relying on Lombok
  public TenantInfrastructureValidator(EntityManagerFactory entityManagerFactory,
                                       ApplicationContext applicationContext) {
    this.entityManagerFactory = entityManagerFactory;
    this.applicationContext = applicationContext;
  }

  @PostConstruct
  public void validateTenantInfrastructure() {
    logger.info("Validating tenant infrastructure...");

    validateHibernateFilterExists();
    validateCriticalFiltersPresent();

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

  private void validateCriticalFiltersPresent() {
    boolean hasRequestId = !applicationContext.getBeansOfType(RequestIdFilter.class).isEmpty();
    boolean hasTenantResolution = !applicationContext.getBeansOfType(TenantResolutionFilter.class).isEmpty();

    if (!hasRequestId) {
      throw new IllegalStateException("RequestIdFilter bean is missing. It must be registered to populate MDC requestId.");
    }
    if (!hasTenantResolution) {
      throw new IllegalStateException("TenantResolutionFilter bean is missing. It must be registered to establish TenantContext per request.");
    }

    logger.info("✓ Critical filters present: RequestIdFilter and TenantResolutionFilter");
  }
}
