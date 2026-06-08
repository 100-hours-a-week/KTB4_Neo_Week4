package com.ktb.community.controller;

import com.ktb.community.common.ApiResponse;
import com.ktb.community.dto.comments.CommentListResponseDto;
import com.ktb.community.dto.comments.CommentRequestDto;
import com.ktb.community.dto.comments.CommentResponseDto;
import com.ktb.community.dto.comments.CommentUpdateResponseDto;
import com.ktb.community.entity.User;
import com.ktb.community.service.AuthService;
import com.ktb.community.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;
    private final AuthService authService;

    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<ApiResponse<CommentResponseDto>> createComment(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long postId,
            @Valid @RequestBody CommentRequestDto request
    ) {
        User loginUser = authService.getLoginUser(authorization);
        CommentResponseDto response = commentService.createComment(loginUser, postId, request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>("create_comment_success", response));
    }

    @PostMapping("/comments/{commentId}/replies")
    public ResponseEntity<ApiResponse<CommentResponseDto>> createReply(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @Valid @RequestBody CommentRequestDto request
    ) {
        User loginUser = authService.getLoginUser(authorization);
        CommentResponseDto response = commentService.createReply(loginUser, postId, commentId, request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>("create_reply_success", response));
    }

    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<ApiResponse<List<CommentListResponseDto>>> getCommentsList(
            @PathVariable Long postId
    ) {
        List<CommentListResponseDto> response = commentService.getCommentsList(postId);

        return ResponseEntity.ok(
                new ApiResponse<>("get_comments_success", response)
        );
    }

    @PatchMapping("/comments/{commentId}")
    public ResponseEntity<ApiResponse<CommentUpdateResponseDto>> updateComment(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long commentId,
            @Valid @RequestBody CommentRequestDto request
    ) {
        User loginUser = authService.getLoginUser(authorization);
        CommentUpdateResponseDto response = commentService.updateComment(loginUser, commentId, request);

        return ResponseEntity.ok(
                new ApiResponse<>("update_comment_success", response)
        );
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<ApiResponse<Boolean>> deleteComment(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long commentId
    ) {
        User loginUser = authService.getLoginUser(authorization);
        commentService.deleteComment(loginUser, commentId);

        return ResponseEntity.ok(
                new ApiResponse<>("delete_comment_success", true)
        );
    }
}