package com.rhettharrison.cms.platform.web.filter;

import com.rhettharrison.cms.platform.common.tenant.TenantContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import org.slf4j.MDC;

@Component
public class TenantResolutionFilter extends OncePerRequestFilter {

  @Override
  protected void doFilterInternal(HttpServletRequest request,
                                  HttpServletResponse response,
                                  FilterChain filterChain) throws ServletException, IOException {

    try {
      // Resolve tenant from subdomain
      String host = request.getHeader("Host"); // e.g., tenant1.example.com
      String tenantId = resolveTenantFromHost(host);

      if (tenantId == null) {
        response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Tenant could not be resolved");
        return;
      }

      // Set tenant in request-scoped context
      TenantContext.setTenantId(tenantId);
      MDC.put("tenantId", TenantContext.getTenantId());

      filterChain.doFilter(request, response);
    } finally {
      // Always clear after request
      TenantContext.clear();
      MDC.remove("tenantId");
    }
  }

  private String resolveTenantFromHost(String host) {
    if (host == null || host.isEmpty()) return null;

    String[] parts = host.split("\\.");
    if (parts.length < 2) {
      // fallback to "default" tenant for localhost/testing
      return "default";
    }

    return parts[0];
  }

}
