package com.rhettharrison.cms.platform.web.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
public class RequestIdFilter extends OncePerRequestFilter {

  public static final String REQUEST_ID_HEADER = "X-Request-ID";

  @Override
  protected void doFilterInternal(HttpServletRequest request,
                                  HttpServletResponse response,
                                  FilterChain filterChain) throws ServletException, IOException {

    String requestId = request.getHeader(REQUEST_ID_HEADER);
    if (requestId == null || requestId.isEmpty()) {
      requestId = UUID.randomUUID().toString();
    }

    // Store in request attribute for downstream use
    request.setAttribute(REQUEST_ID_HEADER, requestId);

    // Add to response header
    response.setHeader(REQUEST_ID_HEADER, requestId);

    // Put in MDC for logging
    MDC.put("requestId", requestId);

    try {
      filterChain.doFilter(request, response);
    } finally {
      // Clear MDC to avoid leaking to other requests
      MDC.clear();
    }
  }
}
