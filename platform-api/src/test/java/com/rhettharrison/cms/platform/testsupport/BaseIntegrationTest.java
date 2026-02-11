package com.rhettharrison.cms.platform.testsupport;

import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@EnabledIfEnvironmentVariable(named = "RUN_IT", matches = "true")
@TestInstance(Lifecycle.PER_CLASS)
public abstract class BaseIntegrationTest {

  // Single reusable PostgreSQL container for the test JVM
  private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16")
      .withDatabaseName("platform_test")
      .withUsername("platform")
      .withPassword("platform");

  static {
    // Explicitly start the container before Spring resolves DynamicPropertySource suppliers
    POSTGRES.start();
  }

  @DynamicPropertySource
  static void registerDatasource(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
    registry.add("spring.datasource.username", POSTGRES::getUsername);
    registry.add("spring.datasource.password", POSTGRES::getPassword);
    // Ensure Flyway is enabled and schema validated during ITs
    registry.add("spring.flyway.enabled", () -> "true");
    registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
  }
}
