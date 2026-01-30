package com.rhettharrison.cms.platform.common.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class PathNormalizerTest {

  @Test
  void normalize_handlesLeadingAndTrailingSlashAndCase() {
    assertEquals("/blog", PathNormalizer.normalize("Blog/"));
    assertEquals("/", PathNormalizer.normalize("/"));
    assertEquals("/hello", PathNormalizer.normalize("hello"));
  }

  @Test
  void normalize_collapsesSlashesAndBackslashes() {
    assertEquals("/a/b", PathNormalizer.normalize("/a//b"));
    assertEquals("/a/b", PathNormalizer.normalize("\\a\\b\\"));
  }

  @Test
  void normalize_resolvesDotAndDotDot() {
    assertEquals("/a/b", PathNormalizer.normalize("/a/./b/"));
    assertEquals("/a", PathNormalizer.normalize("/a/b/../"));
    assertEquals("/", PathNormalizer.normalize("/../../"));
  }
}
