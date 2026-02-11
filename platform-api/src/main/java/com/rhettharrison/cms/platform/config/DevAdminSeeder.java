package com.rhettharrison.cms.platform.config;

import com.rhettharrison.cms.platform.common.tenant.TenantContext;
import com.rhettharrison.cms.platform.domain.model.Tenant;
import com.rhettharrison.cms.platform.domain.model.TenantRepository;
import com.rhettharrison.cms.platform.domain.model.user.User;
import com.rhettharrison.cms.platform.domain.model.user.UserRepository;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Seeds a default admin user for the "default" tenant in local/dev profiles only.
 * Idempotent: if the user exists for the tenant, it does nothing.
 */
@Configuration
@Profile({"local", "dev"})
public class DevAdminSeeder {

  private static final Logger log = LoggerFactory.getLogger(DevAdminSeeder.class);

  @Value("${seed.admin.username:admin}")
  private String seedUsername;

  @Value("${seed.admin.password:password}")
  private String seedPassword;

  @Value("${seed.admin.roles:ROLE_ADMIN}")
  private String seedRoles;

  @Bean
  public ApplicationRunner seedDefaultAdmin(
      TenantRepository tenantRepository,
      UserRepository userRepository,
      PasswordEncoder passwordEncoder
  ) {
    return args -> {
      Optional<Tenant> defaultTenantOpt = tenantRepository.findBySlug("default");
      if (defaultTenantOpt.isEmpty()) {
        log.info("DevAdminSeeder: default tenant not found; skipping admin seed");
        return;
      }

      Tenant defaultTenant = defaultTenantOpt.get();
      UUID tenantId = defaultTenant.getId();

      // Use explicit tenant-scoped lookup since Hibernate tenant filter isn't active at startup
      Optional<User> existing = userRepository.findByTenantIdAndUsernameIgnoreCase(tenantId, seedUsername);
      if (existing.isPresent()) {
        log.info("DevAdminSeeder: admin user '{}' already exists for default tenant; skipping", seedUsername);
        return;
      }

      String passwordHash = passwordEncoder.encode(seedPassword);

      // Set TenantContext so TenantEntityListener assigns tenant_id on persist
      TenantContext.setTenantId(tenantId);
      try {
        User u = new User();
        u.setUsername(seedUsername);
        u.setPasswordHash(passwordHash);
        u.setRoles(seedRoles);
        userRepository.save(u);
        log.warn("DevAdminSeeder: created admin user '{}' for default tenant with a DEVELOPMENT password. Change in local env!", seedUsername);
      } finally {
        TenantContext.clear();
      }
    };
  }
}
