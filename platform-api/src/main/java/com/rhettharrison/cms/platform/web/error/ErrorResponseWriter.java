package com.rhettharrison.cms.platform.web.error;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rhettharrison.cms.platform.common.tenant.TenantContext;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.slf4j.MDC;

public final class ErrorResponseWriter {
  private static final ObjectMapper mapper = new ObjectMapper();

  private ErrorResponseWriter() {}

  public static void write(HttpServletResponse response, int status, String code, String message) throws IOException {
    String traceId = MDC.get("requestId");
    UUID tenantId = TenantContext.getTenantIdOrNull();
    ErrorResponse body = new ErrorResponse(code, message, traceId, tenantId != null ? tenantId.toString() : null);

    response.setStatus(status);
    response.setCharacterEncoding(StandardCharsets.UTF_8.name());
    response.setContentType("application/json");
    mapper.writeValue(response.getOutputStream(), body);
  }
}
