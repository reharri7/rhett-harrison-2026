package com.rhettharrison.cms.platform.common.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class DomainNormalizerTest {

  @Test
  void normalizeHostHeader_basicLowercaseAndStripPort() {
    assertEquals("example.com", DomainNormalizer.normalizeHostHeader("Example.COM:8080"));
  }

  @Test
  void normalizeHostHeader_trailingDotRemoved() {
    assertEquals("example.com", DomainNormalizer.normalizeHostHeader("example.com."));
  }

  @Test
  void normalizeHostHeader_invalidWithSchemeOrPathReturnsNull() {
    assertNull(DomainNormalizer.normalizeHostHeader("http://example.com"));
    assertNull(DomainNormalizer.normalizeHostHeader("example.com/path"));
    // internal whitespace should be rejected
    assertNull(DomainNormalizer.normalizeHostHeader("example.com path"));
    assertNull(DomainNormalizer.normalizeHostHeader("example.com\\back"));
  }

  @Test
  void normalizeHostHeader_idnConvertedToPunycode() {
    String host = "münich.example.com"; // contains umlaut
    String normalized = DomainNormalizer.normalizeHostHeader(host);
    assertEquals("xn--mnich-kva.example.com", normalized);
  }
}
