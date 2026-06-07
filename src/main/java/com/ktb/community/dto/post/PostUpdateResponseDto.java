package com.ktb.community.dto.post;

import lombok.Getter;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class PostUpdateResponseDto {

    private Long postId;
    private boolean isEdited;
    private LocalDateTime editedAt;
}
