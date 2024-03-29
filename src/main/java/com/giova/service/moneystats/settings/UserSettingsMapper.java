package com.giova.service.moneystats.settings;

import com.giova.service.moneystats.authentication.entity.UserEntity;
import com.giova.service.moneystats.settings.dto.UserSettingDTO;
import com.giova.service.moneystats.settings.entity.UserSettingEntity;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

@Component
public class UserSettingsMapper {

  public UserSettingEntity fromUserSettingDTOToEntity(
      UserSettingDTO userSettingDTO, UserEntity user) {
    UserSettingEntity userSettingEntity = new UserSettingEntity();
    BeanUtils.copyProperties(userSettingDTO, userSettingEntity);
    userSettingEntity.setUser(user);
    return userSettingEntity;
  }

  public UserSettingDTO fromUserSettingsEntityToDTO(UserSettingEntity userSettingEntity) {
    UserSettingDTO user = new UserSettingDTO();
    BeanUtils.copyProperties(userSettingEntity, user);
    return user;
  }
}
