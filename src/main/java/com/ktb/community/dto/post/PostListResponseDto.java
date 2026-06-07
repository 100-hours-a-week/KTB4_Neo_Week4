package com.ktb.community.dto.post;

import lombok.Getter;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class PostListResponseDto {

    private Long postId;
    private String title;
    private Long userId;
    private String nickname;
    private String profileImage;
    private LocalDateTime createdAt;
    private int likes;
    private int comments;
    private int views;
    private boolean isBlinded;
}
