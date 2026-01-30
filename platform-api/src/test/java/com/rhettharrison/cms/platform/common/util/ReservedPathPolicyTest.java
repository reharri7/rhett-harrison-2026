package com.rhettharrison.cms.platform.common.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ReservedPathPolicyTest {

  @Test
  void isReserved_deniesAdminAndUnderscorePaths() {
    assertTrue(ReservedPathPolicy.isReserved("/admin"));
    assertTrue(ReservedPathPolicy.isReserved("/admin/settings"));
    assertTrue(ReservedPathPolicy.isReserved("/_api"));
    assertTrue(ReservedPathPolicy.isReserved("/_assets/img"));
  }

  @Test
  void isReserved_allowsNormalPaths() {
    assertFalse(ReservedPathPolicy.isReserved("/"));
    assertFalse(ReservedPathPolicy.isReserved("/blog"));
    assertFalse(ReservedPathPolicy.isReserved("/posts/hello"));
  }
}
