package com.rhettharrison.cms.platform.common.util;

public final class ReservedPathPolicy {
  private ReservedPathPolicy() {}

  public static boolean isReserved(String normalizedPath) {
    if (normalizedPath == null) return true;
    // Basic initial denylist; can be extended/configured later
    if ("/admin".equals(normalizedPath) || normalizedPath.startsWith("/admin/")) return true;
    if (normalizedPath.startsWith("/_")) return true; // e.g., /_api, /_assets
    return false;
  }
}
