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
    private Integer money; // int에서 Integer로 변경

    public static UserDto toUserDto(UserEntity userEntity){
        UserDto userDto = new UserDto();
        userDto.setId(userEntity.getId());
        userDto.setUserId(userEntity.getUserId());
        userDto.setPassword(userEntity.getPassword());
        userDto.setMoney(userEntity.getMoney()); // 수정: UserEntity에서 money 값을 가져오도록

        return userDto;
    }
}
