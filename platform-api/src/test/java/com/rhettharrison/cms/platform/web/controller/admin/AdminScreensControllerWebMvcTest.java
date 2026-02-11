package com.rhettharrison.cms.platform.web.controller.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rhettharrison.cms.platform.domain.model.screen.Screen;
import com.rhettharrison.cms.platform.domain.model.screen.ScreenRepository;
import com.rhettharrison.cms.platform.domain.model.screen.ScreenStatus;
import com.rhettharrison.cms.platform.domain.model.screen.ScreenType;
import com.rhettharrison.cms.platform.web.dto.ScreenDto;
import com.rhettharrison.cms.platform.web.mapper.ScreenMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AdminScreensControllerWebMvcTest {

  private MockMvc mockMvc;
  private final ObjectMapper mapper = new ObjectMapper();

  private final ScreenRepository screenRepository = Mockito.mock(ScreenRepository.class);
  private final ScreenMapper screenMapper = Mockito.mock(ScreenMapper.class);

  @BeforeEach
  void setup() {
    AdminScreensController controller = new AdminScreensController(screenRepository, screenMapper);
    this.mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
  }

  @Test
  void create_markdown_success_returns201AndDto() throws Exception {
    UUID id = UUID.randomUUID();
    // Save returns entity with id set
    when(screenRepository.save(any())).thenAnswer(inv -> {
      Screen s = inv.getArgument(0);
      s.setId(id);
      return s;
    });

    ScreenDto dto = new ScreenDto();
    dto.setId(id);
    dto.setPath("/about");
    dto.setType(ScreenType.MARKDOWN);
    dto.setStatus(ScreenStatus.DRAFT);
    when(screenMapper.toDto(any(Screen.class))).thenReturn(dto);

    String body = "{" +
        "\"path\":\"/about\"," +
        "\"type\":\"MARKDOWN\"," +
        "\"status\":\"DRAFT\"," +
        "\"content\":\"{}\"" +
        "}";

    mockMvc.perform(post("/api/v1/admin/screens")
            .header("Host", "default.yourblog.com")
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(id.toString()))
        .andExpect(jsonPath("$.path").value("/about"));

    // Verify path normalized and fields set prior to save
    ArgumentCaptor<Screen> captor = ArgumentCaptor.forClass(Screen.class);
    Mockito.verify(screenRepository).save(captor.capture());
    Screen saved = captor.getValue();
    assertThat(saved.getPath()).isEqualTo("/about");
    assertThat(saved.getType()).isEqualTo(ScreenType.MARKDOWN);
    assertThat(saved.getStatus()).isEqualTo(ScreenStatus.DRAFT);
    assertThat(saved.getContent()).isNotNull();
  }

  @Test
  void create_redirect_missingUrl_returns400() throws Exception {
    String body = "{" +
        "\"path\":\"/go\"," +
        "\"type\":\"REDIRECT\"," +
        "\"status\":\"PUBLISHED\"" +
        "}";

    mockMvc.perform(post("/api/v1/admin/screens")
            .header("Host", "default.yourblog.com")
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
        .andExpect(status().isBadRequest());
  }

  @Test
  void update_notFound_returns404() throws Exception {
    UUID id = UUID.randomUUID();
    when(screenRepository.findById(id)).thenReturn(Optional.empty());

    String body = "{" +
        "\"path\":\"/new\"" +
        "}";

    mockMvc.perform(put("/api/v1/admin/screens/" + id)
            .header("Host", "default.yourblog.com")
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
        .andExpect(status().isNotFound());
  }

  @Test
  void delete_notFound_returns404() throws Exception {
    UUID id = UUID.randomUUID();
    when(screenRepository.findById(id)).thenReturn(Optional.empty());

    mockMvc.perform(delete("/api/v1/admin/screens/" + id)
            .header("Host", "default.yourblog.com"))
        .andExpect(status().isNotFound());
  }

  @Test
  void delete_success_returns204() throws Exception {
    UUID id = UUID.randomUUID();
    Screen s = new Screen();
    s.setId(id);
    when(screenRepository.findById(id)).thenReturn(Optional.of(s));

    mockMvc.perform(delete("/api/v1/admin/screens/" + id)
            .header("Host", "default.yourblog.com"))
        .andExpect(status().isNoContent());
  }
}
