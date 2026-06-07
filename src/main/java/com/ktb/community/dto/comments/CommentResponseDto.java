package com.ktb.community.dto.comments;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class CommentResponseDto {

    private Long commentId;
    private Long parentCommentId;
    private Long userId;
    private String nickname;
    private String profileImage;
    private String commentBody;
    private LocalDateTime createdAt;
}
