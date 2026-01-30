package com.rhettharrison.cms.platform.domain.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "tenants")
@Getter
@Setter
public class Tenant {

  @Id
  @GeneratedValue
  @Column(columnDefinition = "uuid", updatable = false, nullable = false)
  private UUID id;

  @Column(nullable = false, unique = true, length = 64)
  private String slug;

  @Column(nullable = false)
  private String name;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @PrePersist
  void onCreate() {
    this.createdAt = Instant.now();
  }

  protected Tenant() {} // JPA

}
