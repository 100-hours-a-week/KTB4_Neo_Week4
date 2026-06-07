package com.ktb.community.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "postEditHistory")
public class PostEditHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long historyId;

    private Long postId;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String postBody;

    private String postImage;

    private LocalDateTime createdAt;

    public PostEditHistory(Post post) {
        this.postId = post.getPostId();
        this.title = post.getTitle();
        this.postBody = post.getPostBody();
        this.postImage = post.getPostImage();
        this.createdAt = LocalDateTime.now();
    }
}