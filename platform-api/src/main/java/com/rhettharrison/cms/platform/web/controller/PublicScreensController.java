package com.rhettharrison.cms.platform.web.controller;

import com.rhettharrison.cms.platform.common.util.PathNormalizer;
import com.rhettharrison.cms.platform.domain.model.screen.Screen;
import com.rhettharrison.cms.platform.domain.model.screen.ScreenRepository;
import com.rhettharrison.cms.platform.domain.model.screen.ScreenStatus;
import com.rhettharrison.cms.platform.web.dto.ScreenDto;
import com.rhettharrison.cms.platform.web.mapper.ScreenMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.rhettharrison.cms.platform.web.error.ErrorResponse;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/screens")
@RequiredArgsConstructor
@Tag(name = "Public", description = "Public read APIs that require no authentication")
public class PublicScreensController {

  private final ScreenRepository screenRepository;
  private final ScreenMapper screenMapper;

  @GetMapping
  @Operation(
      summary = "Get published screen by path for current tenant",
      description = "Returns the published screen for the resolved tenant and given path. "
          + "Send a Host header for tenant resolution (e.g., Host: default.yourblog.com). "
          + "Example curl: curl -s 'http://localhost:8080/api/v1/screens?path=/' -H 'Host: default.yourblog.com'"
  )
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "200",
          description = "Published screen found",
          content = @Content(
              schema = @Schema(implementation = ScreenDto.class),
              examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                  name = "OK",
                  value = "{\n  \"id\": \"11111111-1111-1111-1111-111111111111\",\n  \"path\": \"/\",\n  \"type\": \"MARKDOWN\",\n  \"status\": \"PUBLISHED\",\n  \"content\": \"{\\\"markdown\\\":\\\"# Hello\\\"}\",\n  \"publishedAt\": \"2026-01-01T12:00:00Z\"\n}"
              )
          )
      ),
      @ApiResponse(
          responseCode = "400",
          description = "Invalid path",
          content = @Content(
              schema = @Schema(implementation = ErrorResponse.class),
              examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                  name = "Bad Request",
                  value = "{\n  \"code\": \"BAD_REQUEST\",\n  \"message\": \"Invalid path\",\n  \"traceId\": \"c1a2b3...\",\n  \"tenantId\": \"<resolved-tenant-uuid>\"\n}"
              )
          )
      ),
      @ApiResponse(
          responseCode = "404",
          description = "Screen not found or not published",
          content = @Content
      )
  })
  public ResponseEntity<?> getByPath(
      @Parameter(description = "Path of the screen (e.g., / or /about)")
      @RequestParam(name = "path") String pathParam
  ) {
    String path = PathNormalizer.normalize(pathParam);
    if (path == null) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid path");
    }

    Optional<Screen> screenOpt = screenRepository.findByPathAndStatus(path, ScreenStatus.PUBLISHED);
    if (screenOpt.isEmpty()) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    ScreenDto dto = screenMapper.toDto(screenOpt.get());
    return ResponseEntity.ok(dto);
  }
}
