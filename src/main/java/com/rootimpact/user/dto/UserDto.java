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
    private String nickname;
    private String phone;


    public static UserDto toUserDto(UserEntity userEntity){
        UserDto userDto = new UserDto();
        userDto.setId(userEntity.getId());
        userDto.setPhone(userEntity.getPhone());
        userDto.setNickname(userEntity.getNickname());

        return userDto;
    }

}
