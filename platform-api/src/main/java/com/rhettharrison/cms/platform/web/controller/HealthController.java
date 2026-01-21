package com.rhettharrison.cms.platform.web.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/health")
public class HealthController {

  @GetMapping
  public ResponseEntity<String> health() {
    return ResponseEntity.ok("OK");
  }
}
