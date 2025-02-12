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

    public boolean checkDuplicatedPhone(String phone) {
        return userRepository.findByPhone(phone).isPresent();
    }

    public void signUp(UserDto userDto) {
        UserEntity userEntity = UserEntity.toSaveEntity(userDto);
        userRepository.save(userEntity);
    }

    public boolean checkLogin(String phone) {
        Optional<UserEntity> userEntity = userRepository.findByPhone(phone);
        return userEntity.isPresent(); // 유저가 있으면 true, 없으면 false
    }

}
