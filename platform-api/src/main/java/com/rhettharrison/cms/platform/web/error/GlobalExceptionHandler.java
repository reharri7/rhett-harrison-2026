package com.rhettharrison.cms.platform.web.error;

import com.rhettharrison.cms.platform.common.tenant.TenantContext;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.UUID;

@ControllerAdvice
public class GlobalExceptionHandler {

  private String traceId() {
    return MDC.get("requestId");
  }

  private String tenantId() {
    UUID id = TenantContext.getTenantIdOrNull();
    return id != null ? id.toString() : null;
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
    String message = ex.getBindingResult().getFieldErrors().stream()
        .findFirst()
        .map(FieldError::getDefaultMessage)
        .orElse("Validation error");
    ErrorResponse body = new ErrorResponse("VALIDATION_ERROR", message, traceId(), tenantId());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
  }

  @ExceptionHandler(AuthenticationException.class)
  public ResponseEntity<ErrorResponse> handleAuth(AuthenticationException ex) {
    ErrorResponse body = new ErrorResponse("UNAUTHENTICATED", ex.getMessage(), traceId(), tenantId());
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) {
    ErrorResponse body = new ErrorResponse("FORBIDDEN", ex.getMessage(), traceId(), tenantId());
    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
    ErrorResponse body = new ErrorResponse("BAD_REQUEST", ex.getMessage(), traceId(), tenantId());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
    ErrorResponse body = new ErrorResponse("INTERNAL_ERROR", "An unexpected error occurred", traceId(), tenantId());
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .header(HttpHeaders.CONNECTION, "close")
        .body(body);
  }
}
