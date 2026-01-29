package com.rhettharrison.cms.platform.common.tenant;

import jakarta.persistence.EntityManager;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.hibernate.Session;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TenantFilterInterceptor implements HandlerInterceptor {

  private final EntityManager entityManager;

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
    // Only enable filter if tenant context is available
    UUID tenantId = TenantContext.getTenantIdOrNull();
    if (tenantId != null) {
      Session session = entityManager.unwrap(Session.class);
      session.enableFilter("tenantFilter").setParameter("tenantId", tenantId);
    }
    return true;
  }
}
