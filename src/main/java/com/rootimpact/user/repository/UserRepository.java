package com.rootimpact.user.repository;

import com.rootimpact.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Integer> {
    Optional<UserEntity> findByNickname(String nickname);
    Optional<UserEntity> findByPhone(String phone);
}
