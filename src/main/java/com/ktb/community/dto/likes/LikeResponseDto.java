package com.ktb.community.dto.likes;

import lombok.Getter;
import lombok.AllArgsConstructor;

@Getter
@AllArgsConstructor
public class LikeResponseDto {

    private Long postId;
    private boolean isLiked;
    private int likes;
}
