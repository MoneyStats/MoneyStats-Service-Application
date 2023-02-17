package com.giova.service.moneystats.authentication;

import com.giova.service.moneystats.authentication.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IAuthDAO extends JpaRepository<UserEntity, Long> {

  UserEntity findUserEntityByUsername(String username);
}
