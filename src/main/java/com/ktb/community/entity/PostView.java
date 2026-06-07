package com.ktb.community.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "postViews")
public class PostView {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long postViewId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "postId")
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId")
    private User user;

    private LocalDateTime viewedAt;

    public PostView(Post post, User user) {
        this.post = post;
        this.user = user;
        this.viewedAt = LocalDateTime.now();
    }

    public void updateViewedAt() {
        this.viewedAt = LocalDateTime.now();
    }

}
