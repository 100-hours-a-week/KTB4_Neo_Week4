package com.ktb.community.dto.comments;

import com.ktb.community.entity.Comment;
import lombok.Getter;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class CommentListResponseDto {

    private Long commentId;
    private Long userId;
    private String nickname;
    private String profileImage;
    private String commentBody;
    private boolean isDeleted;
    private LocalDateTime createdAt;
    private List<ReplyResponseDto> replies;

    public CommentListResponseDto(Comment comment, List<ReplyResponseDto> replies) {

        this.commentId = comment.getCommentId();
        this.userId = comment.getUser().getUserId();
        this.nickname = comment.getUser().getNickname();
        this.profileImage = comment.getUser().getProfileImage();
        this.commentBody = comment.getCommentBody();
        this.isDeleted = comment.isDeleted();
        this.createdAt = comment.getCreatedAt();
        this.replies = replies;
    }

    @Getter
    @AllArgsConstructor
    public static class ReplyResponseDto {

        private Long commentId;
        private Long userId;
        private String nickname;
        private String profileImage;
        private String commentBody;
        private boolean isDeleted;
        private LocalDateTime createdAt;
    }
}
