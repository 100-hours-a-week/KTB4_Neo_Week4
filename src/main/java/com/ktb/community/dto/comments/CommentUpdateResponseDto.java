package com.ktb.community.dto.comments;

import lombok.Getter;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class CommentUpdateResponseDto {

    private Long commentId;
    private LocalDateTime updatedAt;
    private boolean isEdited;
}
