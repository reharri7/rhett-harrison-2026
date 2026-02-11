package com.rhettharrison.cms.platform.web.controller;

import com.rhettharrison.cms.platform.common.tenant.TenantContext;
import com.rhettharrison.cms.platform.domain.model.user.User;
import com.rhettharrison.cms.platform.domain.model.user.UserRepository;
import com.rhettharrison.cms.platform.security.JwtService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.rhettharrison.cms.platform.web.error.ErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@Tag(name = "Auth", description = "Authentication endpoints")
public class AuthController {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;

  public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.jwtService = jwtService;
  }

  public record LoginRequest(@NotBlank String username, @NotBlank String password) {}

  @PostMapping("/login")
  @Operation(
      summary = "Authenticate and receive a JWT",
      description = "Authenticates the provided credentials within the resolved tenant and returns a Bearer JWT. "
          + "Send a Host header for tenant resolution (e.g., Host: default.yourblog.com). "
          + "Use the returned token as: Authorization: Bearer <token>."
  )
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "200",
          description = "Login successful",
          content = @Content(
              schema = @Schema(implementation = AuthController.LoginSuccess.class),
              examples = @ExampleObject(
                  name = "Success",
                  value = "{\n  \"token\": \"eyJhbGciOiJI...\",\n  \"tokenType\": \"Bearer\",\n  \"issuedAt\": \"2026-01-01T12:00:00Z\"\n}"
              )
          )
      ),
      @ApiResponse(
          responseCode = "401",
          description = "Invalid credentials",
          content = @Content(
              schema = @Schema(implementation = ErrorResponse.class),
              examples = @ExampleObject(
                  name = "Unauthenticated",
                  value = "{\n  \"code\": \"UNAUTHENTICATED\",\n  \"message\": \"Invalid credentials\",\n  \"traceId\": \"c1a2b3...\",\n  \"tenantId\": \"<resolved-tenant-uuid>\"\n}"
              )
          )
      )
  })
  public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req) {
    UUID tenantId = TenantContext.getTenantId();
    // Find user within current tenant (filter enforces tenant automatically)
    User user = userRepository.findByUsernameIgnoreCase(req.username()).orElse(null);
    if (user == null || !passwordEncoder.matches(req.password(), user.getPasswordHash())) {
      return ResponseEntity.status(401).body(Map.of(
          "code", "UNAUTHENTICATED",
          "message", "Invalid credentials"
      ));
    }

    List<String> roles = Arrays.stream(user.getRoles().split(","))
        .map(String::trim)
        .filter(s -> !s.isEmpty())
        .toList();

    String token = jwtService.issueToken(tenantId, user.getUsername(), roles);

    // Simple response with token and metadata
    return ResponseEntity.ok(new LoginSuccess(token, "Bearer", Instant.now().toString()));
  }

  public record LoginSuccess(
      String token,
      String tokenType,
      String issuedAt
  ) {}
}
