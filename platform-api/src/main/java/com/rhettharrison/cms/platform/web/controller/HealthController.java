package com.rhettharrison.cms.platform.web.controller;

import com.rhettharrison.cms.platform.common.tenant.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/health")
public class HealthController {

  private static final Logger log = LoggerFactory.getLogger(HealthController.class);

  @GetMapping
  public ResponseEntity<String> health() {
    // Log request ID and tenant from MDC/Context
    log.info("Health check hit for tenant: {}", TenantContext.getTenantId());
    return ResponseEntity.ok("OK");
  }
}
