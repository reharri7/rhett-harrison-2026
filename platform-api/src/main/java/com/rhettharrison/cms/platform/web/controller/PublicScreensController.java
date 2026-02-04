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
public class PublicScreensController {

  private final ScreenRepository screenRepository;
  private final ScreenMapper screenMapper;

  @GetMapping
  @Operation(summary = "Get published screen by path for current tenant",
      description = "Returns the published screen for the resolved tenant and given path."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Published screen found",
          content = @Content(schema = @Schema(implementation = ScreenDto.class))),
      @ApiResponse(responseCode = "404", description = "Screen not found or not published",
          content = @Content)
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
