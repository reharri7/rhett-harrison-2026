package com.rhettharrison.cms.platform.web.filter;

import com.rhettharrison.cms.platform.common.tenant.TenantContext;
import com.rhettharrison.cms.platform.domain.model.Tenant;
import com.rhettharrison.cms.platform.domain.model.TenantDomain;
import com.rhettharrison.cms.platform.domain.model.TenantDomainRepository;
import com.rhettharrison.cms.platform.domain.model.TenantRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TenantResolutionFilterTest {

  @Mock
  private TenantRepository tenantRepository;

  @Mock
  private TenantDomainRepository tenantDomainRepository;

  @Mock
  private Environment environment;

  @Mock
  private HttpServletRequest request;

  @Mock
  private HttpServletResponse response;

  @Mock
  private FilterChain filterChain;

  private TenantResolutionFilter filter;

  @BeforeEach
  void setUp() {
    filter = new TenantResolutionFilter(tenantRepository, tenantDomainRepository, environment);
    lenient().when(environment.getActiveProfiles()).thenReturn(new String[]{});
  }

  @AfterEach
  void cleanUp() {
    TenantContext.clear();
  }

  @Test
  void shouldNotFilter_shouldBypassForOpenApiAndSwaggerAndHealth() throws Exception {
    // OpenAPI root
    when(request.getRequestURI()).thenReturn("/v3/api-docs");
    assertTrue(filter.shouldNotFilter(request));

    // OpenAPI group/config subpaths
    when(request.getRequestURI()).thenReturn("/v3/api-docs/swagger-config");
    assertTrue(filter.shouldNotFilter(request));

    // Swagger UI
    when(request.getRequestURI()).thenReturn("/swagger-ui/index.html");
    assertTrue(filter.shouldNotFilter(request));

    // Swagger UI legacy
    when(request.getRequestURI()).thenReturn("/swagger-ui.html");
    assertTrue(filter.shouldNotFilter(request));

    // Health
    when(request.getRequestURI()).thenReturn("/health");
    assertTrue(filter.shouldNotFilter(request));

    // A normal API path should not be bypassed
    when(request.getRequestURI()).thenReturn("/api/v1/anything");
    assertFalse(filter.shouldNotFilter(request));
  }

  @Test
  void doFilterInternal_shouldResolveTenantFromDomainTable() throws Exception {
    UUID tenantId = UUID.randomUUID();
    TenantDomain tenantDomain = mock(TenantDomain.class);
    when(tenantDomain.getTenantId()).thenReturn(tenantId);

    when(request.getHeader("Host")).thenReturn("custom.example.com");
    when(tenantDomainRepository.findByDomain("custom.example.com"))
        .thenReturn(Optional.of(tenantDomain));

    filter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    assertNull(TenantContext.getTenantIdOrNull()); // Should be cleared after filter
  }

  @Test
  void doFilterInternal_shouldResolveLocalhostInDevProfile() throws Exception {
    UUID tenantId = UUID.randomUUID();
    Tenant tenant = mock(Tenant.class);
    when(tenant.getId()).thenReturn(tenantId);

    when(request.getHeader("Host")).thenReturn("localhost:8080");
    when(tenantDomainRepository.findByDomain("localhost")).thenReturn(Optional.empty());
    when(environment.getActiveProfiles()).thenReturn(new String[]{"dev"});
    when(tenantRepository.findBySlug("default")).thenReturn(Optional.of(tenant));

    filter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
  }

  @Test
  void doFilterInternal_shouldResolveLocalhostWhenNoProfilesSet() throws Exception {
    UUID tenantId = UUID.randomUUID();
    Tenant tenant = mock(Tenant.class);
    when(tenant.getId()).thenReturn(tenantId);

    when(request.getHeader("Host")).thenReturn("localhost");
    when(tenantDomainRepository.findByDomain("localhost")).thenReturn(Optional.empty());
    when(environment.getActiveProfiles()).thenReturn(new String[]{});
    when(tenantRepository.findBySlug("default")).thenReturn(Optional.of(tenant));

    filter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
  }

  @Test
  void doFilterInternal_shouldResolveFromSubdomain() throws Exception {
    UUID tenantId = UUID.randomUUID();
    Tenant tenant = mock(Tenant.class);
    when(tenant.getId()).thenReturn(tenantId);

    when(request.getHeader("Host")).thenReturn("alice.yourblog.com");
    when(tenantDomainRepository.findByDomain("alice.yourblog.com")).thenReturn(Optional.empty());
    when(tenantRepository.findBySlug("alice")).thenReturn(Optional.of(tenant));

    filter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
  }

  @Test
  void doFilterInternal_shouldNormalizeUppercaseAndPort_thenResolveFromSubdomain() throws Exception {
    UUID tenantId = UUID.randomUUID();
    Tenant tenant = mock(Tenant.class);
    when(tenant.getId()).thenReturn(tenantId);

    when(request.getHeader("Host")).thenReturn("ALICE.Example.COM:443");
    when(tenantDomainRepository.findByDomain("alice.example.com")).thenReturn(Optional.empty());
    when(tenantRepository.findBySlug("alice")).thenReturn(Optional.of(tenant));

    filter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
  }

  @Test
  void doFilterInternal_shouldHandleTrailingDot() throws Exception {
    UUID tenantId = UUID.randomUUID();
    Tenant tenant = mock(Tenant.class);
    when(tenant.getId()).thenReturn(tenantId);

    when(request.getHeader("Host")).thenReturn("alice.example.com.");
    when(tenantDomainRepository.findByDomain("alice.example.com")).thenReturn(Optional.empty());
    when(tenantRepository.findBySlug("alice")).thenReturn(Optional.of(tenant));

    filter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
  }

  @Test
  void doFilterInternal_shouldNormalizeIdnHost() throws Exception {
    UUID tenantId = UUID.randomUUID();
    Tenant tenant = mock(Tenant.class);
    when(tenant.getId()).thenReturn(tenantId);

    // münich.example.com -> xn--mnich-kva.example.com
    when(request.getHeader("Host")).thenReturn("münich.example.com");
    when(tenantDomainRepository.findByDomain("xn--mnich-kva.example.com")).thenReturn(Optional.empty());
    when(tenantRepository.findBySlug("xn--mnich-kva")).thenReturn(Optional.of(tenant));

    filter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
  }

  @Test
  void doFilterInternal_should400OnInvalidHostWithPath() throws Exception {
    when(request.getHeader("Host")).thenReturn("example.com/path");

    filter.doFilterInternal(request, response, filterChain);

    verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Host header is required");
    verify(filterChain, never()).doFilter(request, response);
  }

  @Test
  void doFilterInternal_shouldReturn404ForUnknownTenant() throws Exception {
    when(request.getHeader("Host")).thenReturn("unknown.yourblog.com");
    when(tenantDomainRepository.findByDomain(anyString())).thenReturn(Optional.empty());
    when(tenantRepository.findBySlug(anyString())).thenReturn(Optional.empty());

    filter.doFilterInternal(request, response, filterChain);

    verify(response).sendError(HttpServletResponse.SC_NOT_FOUND, "Tenant not found");
    verify(filterChain, never()).doFilter(request, response);
  }

  @Test
  void doFilterInternal_shouldReturn400ForMissingHostHeader() throws Exception {
    when(request.getHeader("Host")).thenReturn(null);

    filter.doFilterInternal(request, response, filterChain);

    verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Host header is required");
    verify(filterChain, never()).doFilter(request, response);
  }

  @Test
  void doFilterInternal_shouldReturn400ForBlankHostHeader() throws Exception {
    when(request.getHeader("Host")).thenReturn("   ");

    filter.doFilterInternal(request, response, filterChain);

    verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Host header is required");
    verify(filterChain, never()).doFilter(request, response);
  }

  @Test
  void doFilterInternal_shouldStripPortFromHost() throws Exception {
    UUID tenantId = UUID.randomUUID();
    TenantDomain tenantDomain = mock(TenantDomain.class);
    when(tenantDomain.getTenantId()).thenReturn(tenantId);

    when(request.getHeader("Host")).thenReturn("example.com:8080");
    when(tenantDomainRepository.findByDomain("example.com"))
        .thenReturn(Optional.of(tenantDomain));

    filter.doFilterInternal(request, response, filterChain);

    verify(tenantDomainRepository).findByDomain("example.com");
    verify(filterChain).doFilter(request, response);
  }

  @Test
  void doFilterInternal_shouldClearTenantContextEvenOnException() throws Exception {
    UUID tenantId = UUID.randomUUID();
    TenantDomain tenantDomain = mock(TenantDomain.class);
    when(tenantDomain.getTenantId()).thenReturn(tenantId);

    when(request.getHeader("Host")).thenReturn("example.com");
    when(tenantDomainRepository.findByDomain("example.com"))
        .thenReturn(Optional.of(tenantDomain));
    doThrow(new RuntimeException("Test exception")).when(filterChain).doFilter(request, response);

    assertThrows(RuntimeException.class, () ->
        filter.doFilterInternal(request, response, filterChain)
    );

    assertNull(TenantContext.getTenantIdOrNull());
  }
}
