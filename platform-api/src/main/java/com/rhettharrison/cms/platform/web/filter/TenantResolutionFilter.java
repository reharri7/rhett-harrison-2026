package com.rhettharrison.cms.platform.web.filter;

import com.rhettharrison.cms.platform.common.tenant.TenantContext;
import com.rhettharrison.cms.platform.domain.model.Tenant;
import com.rhettharrison.cms.platform.domain.model.TenantDomain;
import com.rhettharrison.cms.platform.domain.model.TenantDomainRepository;
import com.rhettharrison.cms.platform.domain.model.TenantRepository;
import com.rhettharrison.cms.platform.common.util.DomainNormalizer;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TenantResolutionFilter extends OncePerRequestFilter {
  private static final Logger logger = LoggerFactory.getLogger(TenantResolutionFilter.class);

  private final TenantRepository tenantRepository;
  private final TenantDomainRepository tenantDomainRepository;
  private final Environment environment;

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    String path = request.getRequestURI();
    if (path == null) return false;
    return path.startsWith("/v3/api-docs")
        || path.startsWith("/swagger-ui")
        || "/swagger-ui.html".equals(path)
        || "/health".equals(path);
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request,
                                  HttpServletResponse response,
                                  FilterChain filterChain) throws ServletException, IOException {

    try {
      String hostHeader = request.getHeader("Host");
      String domain = DomainNormalizer.normalizeHostHeader(hostHeader);

      if (domain == null || domain.isBlank()) {
        logger.warn("Request received with no Host header");
        response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Host header is required");
        return;
      }

      // Resolve tenant
      Optional<UUID> tenantId = resolveTenantId(domain);

      if (tenantId.isEmpty()) {
        logger.warn("Unknown tenant for domain: {}", domain);
        response.sendError(HttpServletResponse.SC_NOT_FOUND, "Tenant not found");
        return;
      }

      TenantContext.setTenantId(tenantId.get());
      MDC.put("tenantId", tenantId.get().toString());

      filterChain.doFilter(request, response);
    } finally {
      // Always clear after request
      TenantContext.clear();
      MDC.remove("tenantId");
    }
  }

  private Optional<UUID> resolveTenantId(String domain) {
    // 1. Try exact match in tenant_domains table
    Optional<TenantDomain> tenantDomain = tenantDomainRepository.findByDomain(domain);
    if (tenantDomain.isPresent()) {
      return Optional.of(tenantDomain.get().getTenantId());
    }

    // 2. For dev profiles, allow localhost to resolve to default tenant
    if (isDevProfile() && "localhost".equals(domain)) {
      Optional<Tenant> defaultTenant = tenantRepository.findBySlug("default");
      return defaultTenant.map(Tenant::getId);
    }

    // 3. Fallback to subdomain parsing (e.g., alice.yourblog.com -> alice)
    String slug = extractSlugFromDomain(domain);
    if (slug != null) {
      Optional<Tenant> tenant = tenantRepository.findBySlug(slug);
      return tenant.map(Tenant::getId);
    }

    return Optional.empty();
  }

  private String extractSlugFromDomain(String domain) {
    String[] parts = domain.split("\\.");

    // Need at least 3 parts for subdomain (e.g., alice.yourblog.com)
    if (parts.length >= 3) {
      return parts[0].toLowerCase();
    }

    return null;
  }

  private boolean isDevProfile() {
    String[] activeProfiles = environment.getActiveProfiles();
    for (String profile : activeProfiles) {
      if ("dev".equals(profile) || "local".equals(profile)) {
        return true;
      }
    }
    // Default to dev behavior if no profile is set
    return activeProfiles.length == 0;
  }
}
