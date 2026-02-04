package com.rhettharrison.cms.platform.web.mapper;

import com.rhettharrison.cms.platform.domain.model.screen.Screen;
import com.rhettharrison.cms.platform.web.dto.ScreenDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ScreenMapper {
  ScreenDto toDto(Screen s);
}
