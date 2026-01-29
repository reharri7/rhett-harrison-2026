package com.rhettharrison.cms.platform.domain.model.screen;

import com.rhettharrison.cms.platform.common.tenant.TenantAwareEntity;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
    name = "screens",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"tenant_id", "path"})
    }
)
@Getter
@Setter
public class Screen extends TenantAwareEntity {

  @Id
  @GeneratedValue
  @Column(columnDefinition = "uuid", updatable = false, nullable = false)
  private UUID id;

  @Column(nullable = false)
  private String path;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 32)
  private ScreenType type;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 16)
  private ScreenStatus status;

  @Column(columnDefinition = "jsonb", nullable = false)
  private String content;

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

