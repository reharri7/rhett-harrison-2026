package com.rhettharrison.cms.platform.web.dto.admin;

import com.rhettharrison.cms.platform.domain.model.screen.ScreenStatus;
import com.rhettharrison.cms.platform.domain.model.screen.ScreenType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateScreenRequest {
  @NotBlank
  @Size(max = 1024)
  private String path;

  @NotNull
  private ScreenType type;

  @NotNull
  private ScreenStatus status;

  // Required for CONTENT type
  private String content;

  // For REDIRECT type
  private String redirectTargetUrl;
  private Integer redirectStatus;
}
