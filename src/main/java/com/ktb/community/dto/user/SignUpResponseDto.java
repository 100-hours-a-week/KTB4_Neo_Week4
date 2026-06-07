package com.ktb.community.dto.user;

import lombok.Getter;

@Getter
public class SignUpResponseDto {

    private Long userId;

    public SignUpResponseDto(Long userId) {
        this.userId = userId;
    }

}
