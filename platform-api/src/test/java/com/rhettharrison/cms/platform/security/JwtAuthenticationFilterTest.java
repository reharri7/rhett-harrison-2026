package com.rhettharrison.cms.platform.security;

import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.rhettharrison.cms.platform.common.tenant.TenantContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class JwtAuthenticationFilterTest {

  private JwtService jwtService;
  private JwtAuthenticationFilter filter;

  private HttpServletRequest request;
  private HttpServletResponse response;
  private FilterChain chain;

  private final UUID tenantA = UUID.randomUUID();
  private final UUID tenantB = UUID.randomUUID();

  @BeforeEach
  void setup() {
    jwtService = Mockito.mock(JwtService.class);
    filter = new JwtAuthenticationFilter(jwtService);
    request = Mockito.mock(HttpServletRequest.class);
    response = Mockito.mock(HttpServletResponse.class);
    chain = Mockito.mock(FilterChain.class);
    SecurityContextHolder.clearContext();
  }

  @AfterEach
  void tearDown() {
    TenantContext.clear();
    SecurityContextHolder.clearContext();
  }

  @Test
  void mismatchTenant_doesNotAuthenticate() throws Exception {
    TenantContext.setTenantId(tenantA);

    when(request.getRequestURI()).thenReturn("/api/v1/admin/screens");
    when(request.getMethod()).thenReturn("GET");
    when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer token");

    // Mock JWT with different tenant (tenantB)
    DecodedJWT jwt = Mockito.mock(DecodedJWT.class);
    Claim tenantClaim = Mockito.mock(Claim.class);
    when(tenantClaim.asString()).thenReturn(tenantB.toString());
    when(jwt.getClaim("tenant_id")).thenReturn(tenantClaim);
    when(jwt.getSubject()).thenReturn("admin");
    when(jwt.getClaim("roles")).thenReturn(Mockito.mock(Claim.class));
    when(jwtService.verify("token")).thenReturn(jwt);

    filter.doFilterInternal(request, response, chain);

    // Should not set authentication due to tenant mismatch
    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    verify(chain, times(1)).doFilter(request, response);
  }

  @Test
  void matchingTenant_authenticates() throws Exception {
    TenantContext.setTenantId(tenantA);

    when(request.getRequestURI()).thenReturn("/api/secure");
    when(request.getMethod()).thenReturn("GET");
    when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer token");

    // Mock JWT with same tenant
    DecodedJWT jwt = Mockito.mock(DecodedJWT.class);
    Claim tenantClaim = Mockito.mock(Claim.class);
    Claim rolesClaim = Mockito.mock(Claim.class);
    when(tenantClaim.asString()).thenReturn(tenantA.toString());
    when(rolesClaim.asArray(String.class)).thenReturn(new String[]{"ROLE_ADMIN"});
    when(jwt.getClaim("tenant_id")).thenReturn(tenantClaim);
    when(jwt.getClaim("roles")).thenReturn(rolesClaim);
    when(jwt.getSubject()).thenReturn("admin");
    when(jwtService.verify("token")).thenReturn(jwt);

    filter.doFilterInternal(request, response, chain);

    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
    assertThat(SecurityContextHolder.getContext().getAuthentication().getName()).isEqualTo("admin");
    verify(chain, times(1)).doFilter(request, response);
  }
}
