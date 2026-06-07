package com.ktb.community.dto.user;

import lombok.Getter;
import lombok.AllArgsConstructor;

@Getter
@AllArgsConstructor
public class UserResponseDto {

    private Long userId;
    private String nickname;
    private String email;
    private String password;
    private String profileImage;
}
