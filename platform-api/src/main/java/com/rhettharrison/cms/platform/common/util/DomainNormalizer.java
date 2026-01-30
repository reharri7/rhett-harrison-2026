package com.rhettharrison.cms.platform.common.util;

import java.net.IDN;

public final class DomainNormalizer {

  private DomainNormalizer() {}

  public static String normalizeHostHeader(String hostHeader) {
    if (hostHeader == null) return null;
    String host = hostHeader.trim();
    if (host.isEmpty()) return null;

    // Lowercase early and trim trailing dot for uniform checks
    host = host.toLowerCase();
    if (host.endsWith(".")) {
      host = host.substring(0, host.length() - 1);
    }

    // Reject scheme or path fragments quickly (e.g., http://, /path, whitespace, backslashes)
    if (host.contains("://") || host.contains("/") || host.contains(" ") || host.contains("\\")) {
      return null;
    }

    // If a colon remains, it must be a numeric port. Otherwise it's invalid (e.g., http:example)
    int colon = host.indexOf(':');
    if (colon > -1) {
      String port = host.substring(colon + 1);
      if (!port.matches("\\d{1,5}")) {
        return null;
      }
      host = host.substring(0, colon);
    }

    // Convert to ASCII (punycode) to keep storage consistent
    try {
      host = IDN.toASCII(host);
    } catch (Exception ignored) {
      return null;
    }

    return host;
  }
}
