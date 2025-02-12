package com.rootimpact.user.dto;

import com.rootimpact.user.entity.UserEntity;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private Long id;
    private String userId;
    private String password;


    public static UserDto toUserDto(UserEntity userEntity){
        UserDto userDto = new UserDto();
        userDto.setId(userEntity.getId());
        userDto.setUserId(userEntity.getUserId());
        userDto.setPassword(userEntity.getPassword());

        return userDto;
    }

}
