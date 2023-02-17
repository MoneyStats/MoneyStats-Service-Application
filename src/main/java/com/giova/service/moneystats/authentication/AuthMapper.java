package com.giova.service.moneystats.authentication;

import com.giova.service.moneystats.authentication.dto.User;
import com.giova.service.moneystats.authentication.entity.UserEntity;
import io.github.giovannilamarmora.utils.interceptors.LogInterceptor;
import io.github.giovannilamarmora.utils.interceptors.LogTimeTracker;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

@Component
public class AuthMapper {

  @LogInterceptor(type = LogTimeTracker.ActionType.APP_MAPPER)
  public UserEntity mapUserToUserEntity(User user) {
    UserEntity userEntity = new UserEntity();
    BeanUtils.copyProperties(user, userEntity);
    return userEntity;
  }

  @LogInterceptor(type = LogTimeTracker.ActionType.APP_MAPPER)
  public User mapUserEntityToUser(UserEntity userEntity) {
    User user = new User();
    BeanUtils.copyProperties(userEntity, user);
    return user;
  }
}
