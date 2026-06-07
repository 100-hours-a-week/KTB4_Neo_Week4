package com.ktb.community.dto.user;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserUpdateResponseDto {

    private String nickname;
    private String profileImage;
}