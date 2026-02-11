package com.rhettharrison.cms.platform.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rhettharrison.cms.platform.common.tenant.TenantContext;
import com.rhettharrison.cms.platform.domain.model.user.User;
import com.rhettharrison.cms.platform.domain.model.user.UserRepository;
import com.rhettharrison.cms.platform.security.JwtService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerWebMvcTest {

  private MockMvc mockMvc;
  private final ObjectMapper mapper = new ObjectMapper();

  private final UserRepository userRepository = Mockito.mock(UserRepository.class);
  private final PasswordEncoder passwordEncoder = Mockito.mock(PasswordEncoder.class);
  private final JwtService jwtService = Mockito.mock(JwtService.class);

  private final UUID tenantId = UUID.randomUUID();

  @BeforeEach
  void setup() {
    TenantContext.setTenantId(tenantId);
    AuthController controller = new AuthController(userRepository, passwordEncoder, jwtService);
    this.mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
  }

  @AfterEach
  void clear() {
    TenantContext.clear();
  }

  @Test
  void login_success_returnsToken() throws Exception {
    User user = new User();
    user.setRoles("ROLE_ADMIN");
    user.setUsername("admin");
    user.setPasswordHash("$2a$10$dummy");

    when(userRepository.findByUsernameIgnoreCase(eq("admin"))).thenReturn(Optional.of(user));
    when(passwordEncoder.matches(eq("password"), anyString())).thenReturn(true);
    when(jwtService.issueToken(eq(tenantId), eq("admin"), eq(List.of("ROLE_ADMIN")))).thenReturn("test.jwt.token");

    String body = mapper.writeValueAsString(Map.of("username", "admin", "password", "password"));

    mockMvc.perform(post("/auth/login")
            .header("Host", "default.yourblog.com")
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.token").value("test.jwt.token"))
        .andExpect(jsonPath("$.tokenType").value("Bearer"));
  }

  @Test
  void login_invalidCredentials_returns401() throws Exception {
    User user = new User();
    user.setRoles("ROLE_ADMIN");
    user.setUsername("admin");
    user.setPasswordHash("$2a$10$dummy");

    when(userRepository.findByUsernameIgnoreCase(eq("admin"))).thenReturn(Optional.of(user));
    when(passwordEncoder.matches(eq("wrong"), anyString())).thenReturn(false);

    String body = mapper.writeValueAsString(Map.of("username", "admin", "password", "wrong"));

    mockMvc.perform(post("/auth/login")
            .header("Host", "default.yourblog.com")
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
        .andExpect(status().isUnauthorized());
  }
}
