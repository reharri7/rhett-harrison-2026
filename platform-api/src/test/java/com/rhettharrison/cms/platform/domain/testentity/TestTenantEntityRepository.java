package com.rhettharrison.cms.platform.domain.testentity;

import com.rhettharrison.cms.platform.common.repository.TenantAwareRepository;
import java.util.UUID;

public interface TestTenantEntityRepository extends TenantAwareRepository<TestTenantEntity, UUID> {
}
