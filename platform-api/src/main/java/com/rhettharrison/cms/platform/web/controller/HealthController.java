package com.rhettharrison.cms.platform.web.controller;

import com.rhettharrison.cms.platform.common.tenant.TenantContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/health")
@Tag(name = "Health", description = "Liveness/health checks (no auth)")
public class HealthController {

  private static final Logger log = LoggerFactory.getLogger(HealthController.class);

  @GetMapping
  @Operation(summary = "Health check", description = "Returns 200 OK when the service is alive.")
  public ResponseEntity<String> health() {
    // Log request ID and tenant from MDC/Context
    log.info("Health check hit for tenant: {}", TenantContext.getTenantId());
    return ResponseEntity.ok("OK");
  }
}
