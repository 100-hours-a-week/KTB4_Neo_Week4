package com.ktb.community.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "posts")
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long postId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId")
    private User user;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String postBody;

    private String postImage;

    private int likes = 0;
    private int views = 0;
    private int comments = 0;

    private boolean edited = false;
    private boolean deleted = false;
    private boolean blinded = false;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    public Post(User user, String title, String postBody, String postImage) {
        this.user = user;
        this.title = title;
        this.postBody = postBody;
        this.postImage = postImage;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.deletedAt = LocalDateTime.now();
    }

    public void update(String title, String postBody, String postImage) {
        this.title = title;
        this.postBody = postBody;
        this.postImage = postImage;
        this.updatedAt = LocalDateTime.now();
    }

    public void delete() {
        this.deleted = true;
        this.deletedAt = LocalDateTime.now();
    }

    public void increaseViews() {
        this.views++;
    }

    public void increaseLikes() {
        this.likes++;
    }

    public void decreaseLikes() {
        this.likes--;
    }

    public void increaseComments() {
        this.comments++;
    }

    public void blind() {
        this.blinded = true;
    }
}
