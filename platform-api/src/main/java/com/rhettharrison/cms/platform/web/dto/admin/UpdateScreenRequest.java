package com.rhettharrison.cms.platform.web.dto.admin;

import com.rhettharrison.cms.platform.domain.model.screen.ScreenStatus;
import com.rhettharrison.cms.platform.domain.model.screen.ScreenType;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateScreenRequest {
  // All fields optional; controller will update only provided ones

  @Size(max = 1024)
  private String path;

  private ScreenType type;

  private ScreenStatus status;

  private String content;

  private String redirectTargetUrl;
  private Integer redirectStatus;
}
