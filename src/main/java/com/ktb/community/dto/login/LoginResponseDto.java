package com.ktb.community.dto.login;

import lombok.Getter;
import lombok.AllArgsConstructor;

@Getter
@AllArgsConstructor
public class LoginResponseDto {
    private Long userId;
    private String accessToken;
}
