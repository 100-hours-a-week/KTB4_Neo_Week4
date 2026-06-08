package com.ktb.community.service;

import com.ktb.community.dto.comments.CommentListResponseDto;
import com.ktb.community.dto.comments.CommentRequestDto;
import com.ktb.community.dto.comments.CommentResponseDto;
import com.ktb.community.dto.comments.CommentUpdateResponseDto;
import com.ktb.community.entity.Comment;
import com.ktb.community.entity.Post;
import com.ktb.community.entity.User;
import com.ktb.community.exception.ApiException;
import com.ktb.community.repository.CommentRepository;
import com.ktb.community.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;

    public CommentResponseDto createComment(User user, Long postId, CommentRequestDto request) {
        Post post = getActivePost(postId);

        Comment comment = new Comment(
                post,
                user,
                null,
                request.getCommentBody()
        );

        Comment savedComment = commentRepository.save(comment);
        post.increaseComments();

        return toCommentResponse(savedComment);
    }

    public CommentResponseDto createReply(User user, Long postId, Long parentCommentId, CommentRequestDto request) {

        Comment parentComment = commentRepository.findById(parentCommentId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "parent_comment_not_found"));

        Post post = parentComment.getPost();

        if (!post.getPostId().equals(postId)) {
            throw new ApiException(HttpStatus.CONFLICT, "invalid_parent_comment");
        }

        if (post.isDeleted()) {
            throw new ApiException(HttpStatus.NOT_FOUND, "post_not_found");
        }

        if (parentComment.getParentComment() != null) {
            throw new ApiException(HttpStatus.CONFLICT, "reply_depth_exceeded");
        }

        Comment reply = new Comment(
                post,
                user,
                parentComment,
                request.getCommentBody()
        );

        Comment savedReply = commentRepository.save(reply);
        post.increaseComments();

        return toCommentResponse(savedReply);
    }

    @Transactional(readOnly = true)
    public List<CommentListResponseDto> getCommentsList(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "post_not_found"));

        if (post.isDeleted()) {
            throw new ApiException(HttpStatus.NOT_FOUND, "post_not_found");
        }

        return commentRepository.findByPostAndParentCommentIsNull(post)
                .stream()
                .map(comment -> {
                    List<CommentListResponseDto.ReplyResponseDto> replies =
                            commentRepository.findByParentComment(comment)
                                    .stream()
                                    .map(reply -> new CommentListResponseDto.ReplyResponseDto(
                                            reply.getCommentId(),
                                            reply.getUser().getUserId(),
                                            reply.getUser().isDeleted() ? "unknown_user" : reply.getUser().getNickname(),
                                            reply.getUser().isDeleted() ? null : reply.getUser().getProfileImage(),
                                            reply.getCommentBody(),
                                            reply.isDeleted(),
                                            reply.getCreatedAt()
                                    ))
                                    .toList();

                    return new CommentListResponseDto(
                            comment,
                            replies
                    );
                })
                .toList();
    }

    public CommentUpdateResponseDto updateComment(User user, Long commentId, CommentRequestDto request) {
        Comment comment = getComment(commentId);

        validateCommentOwner(user, comment);

        if (comment.isDeleted()) {
            throw new ApiException(HttpStatus.CONFLICT, "deleted_comment_cannot_be_updated");
        }

        if (comment.getCommentBody().equals(request.getCommentBody())) {
            throw new ApiException(HttpStatus.CONFLICT, "comment_content_not_changed");
        }

        comment.update(request.getCommentBody());

        return new CommentUpdateResponseDto(
                comment.getCommentId(),
                comment.getUpdatedAt(),
                true
        );
    }

    public void deleteComment(User user, Long commentId) {
        Comment comment = getComment(commentId);

        validateCommentOwner(user, comment);

        if (comment.isDeleted()) {
            throw new ApiException(HttpStatus.CONFLICT, "already_deleted_comment");
        }

        comment.deleteSoftly();
        comment.getPost().decreaseComments();
    }

    private Comment getComment(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "comment_not_found"));
    }

    private Post getActivePost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "post_not_found"));

        if (post.isDeleted()) {
            throw new ApiException(HttpStatus.NOT_FOUND, "post_not_found");
        }

        return post;
    }

    private void validateCommentOwner(User user, Comment comment) {
        if (!comment.getUser().getUserId().equals(user.getUserId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "denied_access");
        }
    }

    private CommentResponseDto toCommentResponse(Comment comment) {
        return new CommentResponseDto(
                comment.getCommentId(),
                comment.getParentComment() != null ? comment.getParentComment().getCommentId() : null,
                comment.getUser().getUserId(),
                comment.getUser().isDeleted() ? "알 수 없음" : comment.getUser().getNickname(),
                comment.getUser().isDeleted() ? null : comment.getUser().getProfileImage(),
                comment.getCommentBody(),
                comment.getCreatedAt()
        );
    }
}
