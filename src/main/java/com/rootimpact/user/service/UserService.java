package com.rootimpact.user.service;


import com.rootimpact.user.dto.UserDto;
import com.rootimpact.user.entity.UserEntity;
import com.rootimpact.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public boolean checkDuplicatedUserId(String userId) {
        return userRepository.findByUserId(userId).isPresent();
    }

    public void signUp(UserDto userDto) {
        UserEntity userEntity = UserEntity.toSaveEntity(userDto);
        userRepository.save(userEntity);
    }

    public boolean checkLogin(String userId, String password) {
        Optional<UserEntity> userEntity = userRepository.findByUserId(userId);  // userId로 사용자 조회
        if (userEntity.isPresent()) {
            // 비밀번호 확인
            return userEntity.get().getPassword().equals(password);
        }
        return false;  // userId가 없거나 비밀번호가 일치하지 않으면 false 반환
    }


}
