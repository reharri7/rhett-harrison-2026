package com.rhettharrison.cms.platform.domain.model.user;

import com.rhettharrison.cms.platform.common.tenant.TenantAwareEntity;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "users",
    indexes = {
        @Index(name = "uq_users_tenant_username", columnList = "tenant_id,username", unique = true)
    })
@Getter
@Setter
public class User extends TenantAwareEntity {

  @Id
  @GeneratedValue
  @Column(columnDefinition = "uuid", updatable = false, nullable = false)
  private UUID id;

  @Column(nullable = false, length = 128)
  private String username;

  @Column(name = "password_hash", nullable = false, length = 255)
  private String passwordHash;

  // Comma separated roles e.g. ROLE_ADMIN,ROLE_EDITOR
  @Column(nullable = false, length = 255)
  private String roles;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @PrePersist
  void onCreate() {
    this.createdAt = Instant.now();
    this.updatedAt = this.createdAt;
  }

  @PreUpdate
  void onUpdate() {
    this.updatedAt = Instant.now();
  }
}
