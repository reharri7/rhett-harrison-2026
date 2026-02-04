package com.rhettharrison.cms.platform.web.dto;

import com.rhettharrison.cms.platform.domain.model.screen.ScreenStatus;
import com.rhettharrison.cms.platform.domain.model.screen.ScreenType;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ScreenDto {
  private UUID id;
  private String path;
  private ScreenType type;
  private ScreenStatus status;
  private String content;
  private Instant publishedAt;

  // For redirects (optional, may be null)
  private String redirectTargetUrl;
  private Integer redirectStatus;
}
