package com.rhettharrison.cms.platform.common.util;

public final class PathNormalizer {

  private PathNormalizer() {}

  public static String normalize(String input) {
    if (input == null) return null;
    String p = input.trim();
    if (p.isEmpty()) return "/";

    // Ensure leading slash
    if (!p.startsWith("/")) {
      p = "/" + p;
    }

    // Replace backslashes with forward slashes
    p = p.replace('\\', '/');

    // Collapse duplicate slashes
    p = p.replaceAll("/+", "/");

    // Resolve dot segments (basic): remove '/./'
    p = p.replace("/./", "/");

    // Remove any trailing slash except root
    if (p.length() > 1 && p.endsWith("/")) {
      p = p.substring(0, p.length() - 1);
    }

    // Lowercase for case-insensitivity
    p = p.toLowerCase();

    // Prevent going above root using naive '..' removal
    // Safer approach would be to split and rebuild
    String[] parts = p.split("/");
    java.util.Deque<String> stack = new java.util.ArrayDeque<>();
    for (String part : parts) {
      if (part.isEmpty()) continue;
      if (part.equals("..")) {
        if (!stack.isEmpty()) stack.removeLast();
      } else if (!part.equals(".")) {
        stack.addLast(part);
      }
    }
    StringBuilder sb = new StringBuilder("/");
    boolean first = true;
    for (String seg : stack) {
      if (!first) sb.append('/');
      sb.append(seg);
      first = false;
    }
    String result = sb.toString();
    return result.isEmpty() ? "/" : result;
  }
}
