package com.rhettharrison.cms.platform.web.controller.admin;

import com.rhettharrison.cms.platform.common.util.PathNormalizer;
import com.rhettharrison.cms.platform.domain.model.screen.Screen;
import com.rhettharrison.cms.platform.domain.model.screen.ScreenRepository;
import com.rhettharrison.cms.platform.domain.model.screen.ScreenStatus;
import com.rhettharrison.cms.platform.domain.model.screen.ScreenType;
import com.rhettharrison.cms.platform.web.dto.ScreenDto;
import com.rhettharrison.cms.platform.web.dto.admin.CreateScreenRequest;
import com.rhettharrison.cms.platform.web.dto.admin.UpdateScreenRequest;
import com.rhettharrison.cms.platform.web.mapper.ScreenMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/screens")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "Administrative CRUD APIs (JWT required)")
@SecurityRequirement(name = "bearer-jwt")
@PreAuthorize("hasAnyRole('ADMIN','EDITOR')")
public class AdminScreensController {

  private static final Set<Integer> ALLOWED_REDIRECT_STATUSES = Set.of(301, 302, 307, 308);

  private final ScreenRepository screenRepository;
  private final ScreenMapper screenMapper;

  @PostMapping
  @Operation(
      summary = "Create a screen",
      description = "Creates a screen in the resolved tenant. Requires Bearer JWT with ADMIN or EDITOR role. "
          + "Example curl: curl -s -X POST 'http://localhost:8080/api/v1/admin/screens' "
          + "-H 'Authorization: Bearer <token>' -H 'Host: default.yourblog.com' -H 'Content-Type: application/json' "
          + "-d '{\"path\":\"/about\",\"type\":\"MARKDOWN\",\"status\":\"DRAFT\",\"content\":\"{}\"}'"
  )
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "201",
          description = "Screen created",
          content = @Content(
              schema = @Schema(implementation = ScreenDto.class),
              examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                  name = "Created",
                  value = "{\n  \"id\": \"11111111-1111-1111-1111-111111111111\",\n  \"path\": \"/about\",\n  \"type\": \"MARKDOWN\",\n  \"status\": \"DRAFT\"\n}"
              )
          )
      ),
      @ApiResponse(
          responseCode = "400",
          description = "Validation error",
          content = @Content(
              schema = @Schema(implementation = com.rhettharrison.cms.platform.web.error.ErrorResponse.class),
              examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                  name = "Invalid",
                  value = "{\n  \"code\": \"BAD_REQUEST\",\n  \"message\": \"content is required for non-REDIRECT screen types\",\n  \"traceId\": \"c1a2b3...\",\n  \"tenantId\": \"<resolved-tenant-uuid>\"\n}"
              )
          )
      ),
      @ApiResponse(
          responseCode = "401",
          description = "Unauthenticated",
          content = @Content(schema = @Schema(implementation = com.rhettharrison.cms.platform.web.error.ErrorResponse.class))
      ),
      @ApiResponse(
          responseCode = "403",
          description = "Forbidden",
          content = @Content(schema = @Schema(implementation = com.rhettharrison.cms.platform.web.error.ErrorResponse.class))
      )
  })
  public ResponseEntity<?> create(@Valid @RequestBody CreateScreenRequest req) {
    String normalized = PathNormalizer.normalize(req.getPath());
    if (normalized == null) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid path");
    }

    // Validate type-specific requirements
    String validationError = validateTypeSpecific(req.getType(), req.getContent(), req.getRedirectTargetUrl(), req.getRedirectStatus());
    if (validationError != null) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(validationError);
    }

    Screen s = new Screen();
    s.setPath(normalized);
    s.setType(req.getType());
    s.setStatus(req.getStatus());
    s.setContent(defaultContent(req.getType(), req.getContent()));
    s.setRedirectTargetUrl(req.getRedirectTargetUrl());
    s.setRedirectStatus(req.getRedirectStatus());

    Screen saved = screenRepository.save(s);
    ScreenDto dto = screenMapper.toDto(saved);
    return ResponseEntity.created(URI.create("/api/v1/admin/screens/" + saved.getId())).body(dto);
  }

  @PutMapping("/{id}")
  @Operation(
      summary = "Update a screen",
      description = "Updates a screen by id. Requires Bearer JWT with ADMIN or EDITOR role. "
          + "Example curl: curl -s -X PUT 'http://localhost:8080/api/v1/admin/screens/{id}' "
          + "-H 'Authorization: Bearer <token>' -H 'Host: default.yourblog.com' -H 'Content-Type: application/json' "
          + "-d '{\"status\":\"PUBLISHED\"}'"
  )
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "200",
          description = "Screen updated",
          content = @Content(schema = @Schema(implementation = ScreenDto.class))
      ),
      @ApiResponse(
          responseCode = "400",
          description = "Validation error",
          content = @Content(schema = @Schema(implementation = com.rhettharrison.cms.platform.web.error.ErrorResponse.class))
      ),
      @ApiResponse(
          responseCode = "404",
          description = "Screen not found",
          content = @Content
      ),
      @ApiResponse(
          responseCode = "401",
          description = "Unauthenticated",
          content = @Content(schema = @Schema(implementation = com.rhettharrison.cms.platform.web.error.ErrorResponse.class))
      ),
      @ApiResponse(
          responseCode = "403",
          description = "Forbidden",
          content = @Content(schema = @Schema(implementation = com.rhettharrison.cms.platform.web.error.ErrorResponse.class))
      )
  })
  public ResponseEntity<?> update(@PathVariable("id") UUID id, @Valid @RequestBody UpdateScreenRequest req) {
    Optional<Screen> screenOpt = screenRepository.findById(id);
    if (screenOpt.isEmpty()) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    Screen s = screenOpt.get();

    if (req.getPath() != null) {
      String normalized = PathNormalizer.normalize(req.getPath());
      if (normalized == null) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid path");
      }
      s.setPath(normalized);
    }

    if (req.getType() != null) {
      s.setType(req.getType());
    }

    if (req.getStatus() != null) {
      s.setStatus(req.getStatus());
    }

    if (req.getType() != null || req.getContent() != null || req.getRedirectTargetUrl() != null || req.getRedirectStatus() != null) {
      ScreenType type = req.getType() != null ? req.getType() : s.getType();
      String content = req.getContent() != null ? req.getContent() : s.getContent();
      String redirectUrl = req.getRedirectTargetUrl() != null ? req.getRedirectTargetUrl() : s.getRedirectTargetUrl();
      Integer redirectStatus = req.getRedirectStatus() != null ? req.getRedirectStatus() : s.getRedirectStatus();
      String validationError = validateTypeSpecific(type, content, redirectUrl, redirectStatus);
      if (validationError != null) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(validationError);
      }
      s.setContent(defaultContent(type, content));
      s.setRedirectTargetUrl(redirectUrl);
      s.setRedirectStatus(redirectStatus);
    }

    Screen saved = screenRepository.save(s);
    ScreenDto dto = screenMapper.toDto(saved);
    return ResponseEntity.ok(dto);
  }

  @DeleteMapping("/{id}")
  @Operation(
      summary = "Delete a screen",
      description = "Deletes a screen by id. Requires Bearer JWT with ADMIN or EDITOR role. "
          + "Example curl: curl -s -X DELETE 'http://localhost:8080/api/v1/admin/screens/{id}' -H 'Authorization: Bearer <token>' -H 'Host: default.yourblog.com'"
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Deleted"),
      @ApiResponse(responseCode = "404", description = "Screen not found", content = @Content),
      @ApiResponse(responseCode = "401", description = "Unauthenticated", content = @Content(schema = @Schema(implementation = com.rhettharrison.cms.platform.web.error.ErrorResponse.class))),
      @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = com.rhettharrison.cms.platform.web.error.ErrorResponse.class)))
  })
  public ResponseEntity<?> delete(@PathVariable("id") UUID id) {
    Optional<Screen> screenOpt = screenRepository.findById(id);
    if (screenOpt.isEmpty()) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
    screenRepository.deleteById(id);
    return ResponseEntity.noContent().build();
  }

  private String validateTypeSpecific(ScreenType type, String content, String redirectUrl, Integer redirectStatus) {
    if (type == ScreenType.REDIRECT) {
      if (redirectUrl == null || redirectUrl.isBlank()) {
        return "redirectTargetUrl is required for REDIRECT type";
      }
      if (redirectStatus == null || !ALLOWED_REDIRECT_STATUSES.contains(redirectStatus)) {
        return "redirectStatus must be one of 301,302,307,308 for REDIRECT type";
      }
    } else {
      // All non-redirect screen types require content
      if (content == null || content.isBlank()) {
        return "content is required for non-REDIRECT screen types";
      }
    }
    return null;
  }

  private String defaultContent(ScreenType type, String content) {
    if (type == ScreenType.REDIRECT) {
      return content; // ignored for redirect
    }
    return content != null ? content : "{}";
  }
}
