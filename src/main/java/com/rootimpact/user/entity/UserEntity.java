package com.rootimpact.user.entity;

import com.rootimpact.user.dto.UserDto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@Setter
@Table(name = "user")
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String userId;

    @Column(nullable = false, length = 15)
    private String password;

    @Column(nullable = false)
    private Integer money; // Iㅖnteger로 변경

    @PrePersist
    public void prePersist() {
        if (this.money == null) { // null이면 기본값 설정
            this.money = 10000000;
        }
    }

    public static UserEntity toSaveEntity(UserDto userDto){
        return UserEntity.builder()
                .userId(userDto.getUserId())
                .password(userDto.getPassword())
                .money(userDto.getMoney() != null ? userDto.getMoney() : 10000000) // money 기본값 설정
                .build();
    }
}