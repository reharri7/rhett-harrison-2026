package com.rhettharrison.cms.platform.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

  @Bean
  public OpenAPI platformOpenAPI() {
    // Define a reusable Bearer JWT security scheme (can be applied at operation level)
    SecurityScheme bearerJwt = new SecurityScheme()
        .type(SecurityScheme.Type.HTTP)
        .scheme("bearer")
        .bearerFormat("JWT")
        .name("Authorization");

    return new OpenAPI()
        .info(new Info()
            .title("Platform API — Multi‑Tenant CMS")
            .description("Public and administrative APIs for a multi-tenant CMS/blog platform."
                + " Tenancy is resolved from the Host header; all endpoints operate in the resolved tenant context.")
            .version("v1")
            .license(new License().name("MIT"))
            .contact(new Contact().name("Rhett Harrison"))
        )
        .components(new Components().addSecuritySchemes("bearer-jwt", bearerJwt));
  }
}
