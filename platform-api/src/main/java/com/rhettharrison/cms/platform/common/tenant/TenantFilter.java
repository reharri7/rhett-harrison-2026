package com.rhettharrison.cms.platform.common.tenant;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@FilterDef(name = "tenantFilter", parameters = @ParamDef(name = "tenantId", type = java.util.UUID.class))
@org.hibernate.annotations.Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
public @interface TenantFilter {
}
