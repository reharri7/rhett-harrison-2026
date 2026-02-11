package com.rhettharrison.cms.platform.security;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.rhettharrison.cms.platform.common.tenant.TenantContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

  private final JwtService jwtService;

  public JwtAuthenticationFilter(JwtService jwtService) {
    this.jwtService = jwtService;
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    String path = request.getRequestURI();
    if (path == null) return false;
    return path.equals("/health")
        || path.startsWith("/v3/api-docs")
        || path.startsWith("/swagger-ui")
        || path.equals("/swagger-ui.html")
        || ("POST".equalsIgnoreCase(request.getMethod()) && path.equals("/auth/login"))
        || ("GET".equalsIgnoreCase(request.getMethod()) && path.equals("/api/v1/screens"));
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    String auth = request.getHeader(HttpHeaders.AUTHORIZATION);
    if (auth != null && auth.startsWith("Bearer ")) {
      String token = auth.substring(7);
      try {
        DecodedJWT jwt = jwtService.verify(token);
        String tenantIdClaim = jwt.getClaim("tenant_id").asString();
        UUID tokenTenant = tenantIdClaim != null ? UUID.fromString(tenantIdClaim) : null;
        UUID ctxTenant = TenantContext.getTenantIdOrNull();

        if (tokenTenant == null || ctxTenant == null || !tokenTenant.equals(ctxTenant)) {
          // Mismatch → do not authenticate; leave to security to reject
          log.warn("JWT tenant mismatch or missing. tokenTenant={}, ctxTenant={}", tokenTenant, ctxTenant);
        } else {
          String username = jwt.getSubject();
          String[] rolesArray = jwt.getClaim("roles").asArray(String.class);
          List<GrantedAuthority> authorities = rolesArray == null ? List.of() : Arrays.stream(rolesArray)
              .map(SimpleGrantedAuthority::new)
              .map(GrantedAuthority.class::cast)
              .toList();

          UsernamePasswordAuthenticationToken authentication =
              new UsernamePasswordAuthenticationToken(username, null, authorities);
          SecurityContextHolder.getContext().setAuthentication(authentication);
        }
      } catch (Exception e) {
        log.warn("JWT verification failed: {}", e.getMessage());
        // leave unauthenticated; downstream security will return 401 if required
      }
    }

    filterChain.doFilter(request, response);
  }
}
