package com.rhettharrison.cms.platform.web.error;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
    String code,
    String message,
    String traceId,
    String tenantId
) {}
