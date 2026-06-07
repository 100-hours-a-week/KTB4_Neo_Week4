package com.ktb.community.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "comments")
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long commentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "postId")
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parentCommentId")
    private Comment parentComment;

    @Column(columnDefinition = "TEXT")
    private String commentBody;

    private boolean edited = false;
    private boolean deleted = false;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    public Comment(Post post, User user, Comment parentComment, String commentBody) {
        this.post = post;
        this.user = user;
        this.parentComment = parentComment;
        this.commentBody = commentBody;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void update(String commentBody) {
        this.commentBody = commentBody;
        this.edited = true;
        this.updatedAt = LocalDateTime.now();
    }

    public void deleteSoftly() {
        this.deleted = true;
        this.commentBody = "삭제된 댓글입니다.";
        this.deletedAt = LocalDateTime.now();
    }
}
