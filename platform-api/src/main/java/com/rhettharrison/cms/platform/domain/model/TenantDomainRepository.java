package com.rhettharrison.cms.platform.domain.model;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TenantDomainRepository extends JpaRepository<TenantDomain, UUID> {
  Optional<TenantDomain> findByDomain(String domain);
}
